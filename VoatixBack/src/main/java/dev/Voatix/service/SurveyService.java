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
import dev.Voatix.utils.exceptions.commonException.UnknownStatusException;
import dev.Voatix.utils.exceptions.commonException.UserUnauthorizedException;
import dev.Voatix.utils.exceptions.moderatorException.ModeratorAccessDeniedException;
import dev.Voatix.utils.exceptions.projectException.ProjectNotFoundException;
import dev.Voatix.utils.exceptions.surveyException.SurveyAccessDeniedException;
import dev.Voatix.utils.exceptions.surveyException.SurveyNotFoundException;
import dev.Voatix.utils.exceptions.surveyException.SurveyVotingTimeIsUpException;
import dev.Voatix.utils.exceptions.surveyException.VotingPointNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
        Long userId = userRepository.findIdByNickname(principal.getName())
                .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
        TypeOfSurveyEnum status = null;
        if (!filterBy.isBlank() && !filterBy.equals("ALL")) {
            try {
                log.info("\"" + filterBy + "\"");
                status = TypeOfSurveyEnum.valueOf(filterBy);
            } catch (IllegalArgumentException e) {
                throw new UnknownStatusException(filterBy);
            }
        }
        Pageable pageable = PageRequest.of(page, limit);

        Page<SurveyEntity> surveys = surveyRepository.findSurveys(project, status, searchedValue, pageable);

        List<Long> ids = surveys.getContent().stream().map(SurveyEntity::getId).toList();

        List<VotingEstimatesProjection> votingEstimatesProj = surveyRepository.findAllPointsWithVotes(ids, userId);
        return surveyMapper.toPageDto(surveys, votingEstimatesProj);
    }

    public void createSurvey(SurveyCreateDTO dto, Principal principal){
        Long userId = userRepository.findIdByNickname(principal.getName())
                .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
        Long projectId = projectRepository.findIdByTitle(dto.getProjectName())
                .orElseThrow(() -> new ProjectNotFoundException(dto.getProjectName()));
        ModeratorEntity moderator = moderatorRepository.findByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> new ModeratorAccessDeniedException(principal.getName()));
        surveyRepository.save(surveyMapper.toEntity(dto, userId, projectId));
    }

    public void deleteSurvey(Long surveyId, Principal principal){
        UserEntity user = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
        SurveyEntity survey = surveyRepository.findSurveyById(surveyId)
                .orElseThrow(() -> new SurveyNotFoundException(surveyId));
        ModeratorEntity moderator = moderatorRepository.findByUserIdAndProjectId(user.getId(), survey.getProject().getId())
                .orElseThrow(() -> new ModeratorAccessDeniedException(principal.getName()));
        boolean isAuthor = user.getId().equals(survey.getUser().getId());
        boolean isProjectOwner = moderator.getRole().equals(RoleOfProjectManager.OWNER);
        if (!isAuthor && !isProjectOwner && !user.getCredentials().getRole().equals(RoleOfUserEnum.ADMIN)) {
            throw new SurveyAccessDeniedException(principal.getName());
        }
        surveyRepository.delete(survey);
    }

    public void doVote(Long votingPointId, Principal principal){
        UserEntity user = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
        VotingPointEntity votingPoint = votingPointRepository.findById(votingPointId)
                .orElseThrow(() -> new VotingPointNotFoundException(votingPointId));
        PointEstimateEntity pointEstimate = pointEstimateRepository.findByUserIdAndVotingPointId(user.getId(), votingPointId)
                .orElse(null);
        SurveyEntity survey = votingPoint.getSurvey();

        if (survey.getEndDate().isBefore(LocalDateTime.now())){
            throw new SurveyVotingTimeIsUpException();
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
