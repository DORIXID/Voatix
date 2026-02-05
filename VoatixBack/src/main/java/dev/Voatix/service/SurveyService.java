package dev.Voatix.service;

import dev.Voatix.dto.SurveyCreateDTO;
import dev.Voatix.dto.SurveyResponseDTO;
import dev.Voatix.dto.projection.VotingEstimatesProjection;
import dev.Voatix.entity.*;
import dev.Voatix.entity.enums.RoleOfProjectManager;
import dev.Voatix.entity.enums.RoleOfUserEnum;
import dev.Voatix.entity.enums.TypeOfSurveyEnum;
import dev.Voatix.mapper.PointEstimateMapper;
import dev.Voatix.mapper.SurveyMapper;
import dev.Voatix.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final UserRepository userRepository;
    private final SurveyMapper surveyMapper;
    private final ProjectRepository projectRepository;
    private final ModeratorRepository moderatorRepository;
    private final PointEstimateRepository pointEstimateRepository;
    private final PointEstimateMapper pointEstimateMapper;
    private final VotingPointRepository votingPointRepository;

    public Page<SurveyResponseDTO> getSurveys(String project, Integer page, Integer limit, String filterBy, String searchedValue, Principal principal){
        UserEntity user = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        TypeOfSurveyEnum status = null;
        if (!filterBy.isBlank() && !filterBy.equals("ALL")) {
            try {
                log.info("\"" + filterBy + "\"");
                status = TypeOfSurveyEnum.valueOf(filterBy);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown status: " + filterBy);
            }
        }
        Pageable pageable = PageRequest.of(page, limit);

        Page<SurveyEntity> surveys = surveyRepository.findSurveys(project, status, searchedValue, pageable);

        List<Long> ids = surveys.getContent().stream().map(SurveyEntity::getId).toList();

        List<VotingEstimatesProjection> votingEstimatesProj = surveyRepository.findAllPointsWithVotes(ids, user.getId());
        return surveyMapper.toPageDto(surveys, votingEstimatesProj);
    }

    public void createSurvey(SurveyCreateDTO dto, Principal principal){
        UserEntity userEntity = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User \"" + principal.getName() + "\" not found"));
        ProjectEntity projectEntity = projectRepository.findByName(dto.getProjectName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project " + dto.getProjectName() + " not found"));
        ModeratorEntity moderator = moderatorRepository.findByUserIdAndProjectId(userEntity.getId(), projectEntity.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User \"" + principal.getName() + "\" does not have access"));
        surveyRepository.save(surveyMapper.toEntity(dto, userEntity, projectEntity));
    }

    public void deleteSurvey(Long surveyId, Principal principal){
        UserEntity user = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User \"" + principal.getName() + "\" not found"));
        SurveyEntity survey = surveyRepository.findSurveyById(surveyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Survey " + surveyId + " not found"));
        ModeratorEntity moderator = moderatorRepository.findByUserIdAndProjectId(user.getId(), survey.getProject().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User \"" + principal.getName() + "\" does not have access"));
        boolean isAuthor = user.getId().equals(survey.getUser().getId());
        boolean isProjectOwner = moderator.getRole().equals(RoleOfProjectManager.OWNER);
        if (!isAuthor && !isProjectOwner && !user.getCredentials().getRole().equals(RoleOfUserEnum.ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User \"" + principal.getName() + "\" does not have access to delete this survey");
        }
        surveyRepository.delete(survey);
    }

    public void doVote(Long votingPointId, Principal principal){
        UserEntity user = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User \"" + principal.getName() + "\" not found"));
        VotingPointEntity votingPoint = votingPointRepository.findById(votingPointId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voting point " + votingPointId + " not found"));
        PointEstimateEntity pointEstimate = pointEstimateRepository.findByUserIdAndVotingPointId(user.getId(), votingPointId)
                .orElse(null);
        SurveyEntity survey = surveyRepository.findSurveyById(votingPoint.getSurvey().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Survey " + votingPoint.getSurvey().getId() + " not found"));

        if (survey.getEndDate().isBefore(LocalDateTime.now())){
            throw new ResponseStatusException(HttpStatus.GONE, "Voting time is up");
        }

        if (survey.getType().equals(TypeOfSurveyEnum.RADIO_BUTTON) && pointEstimate == null) {
            pointEstimateRepository.findByUserIdAndSurveyId(user.getId(), survey.getId())
                    .ifPresent(pointEstimateRepository::delete);
        }

        if (pointEstimate == null) {
            pointEstimateRepository.save(pointEstimateMapper.toEntity(user, votingPoint));
        } else  {
            pointEstimateRepository.delete(pointEstimate);
        }

    }

}
