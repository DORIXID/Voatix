package dev.Voatix.service;

import dev.Voatix.dto.survey.SurveyCreateDTO;
import dev.Voatix.dto.survey.SurveyResponseDTO;
import dev.Voatix.dto.survey.SurveysRequestDTO;
import dev.Voatix.dto.survey.VotingEstimatesProjection;
import dev.Voatix.entity.*;
import dev.Voatix.entity.enums.RoleOfProjectManager;
import dev.Voatix.entity.enums.TypeOfSurveyEnum;
import dev.Voatix.mapper.PointEstimateMapper;
import dev.Voatix.mapper.SurveyMapper;
import dev.Voatix.repositories.*;
import dev.Voatix.utils.exceptions.commonException.UnknownStatusException;
import dev.Voatix.utils.exceptions.moderatorException.ModeratorAccessDeniedException;
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
    private final ModeratorRepository moderatorRepository;
    private final PointEstimateRepository pointEstimateRepository;
    private final PointEstimateMapper pointEstimateMapper;
    private final VotingPointRepository votingPointRepository;

    public Page<SurveyResponseDTO> getSurveys(SurveysRequestDTO dto, Long userId) {
        TypeOfSurveyEnum status = null;
        if (!dto.getFilterBy().isBlank() && !dto.getFilterBy().equals("ALL")) {
            try {
                status = TypeOfSurveyEnum.valueOf(dto.getFilterBy());
            } catch (IllegalArgumentException e) {
                throw new UnknownStatusException(dto.getFilterBy());
            }
        }
        Pageable pageable = PageRequest.of(dto.getPage(), dto.getLimit());

        Page<SurveyEntity> surveys = surveyRepository.findSurveys(dto.getProjectId(), status, dto.getSearchedValue(), pageable);

        List<Long> ids = surveys.getContent().stream().map(SurveyEntity::getId).toList();

        List<VotingEstimatesProjection> votingEstimatesProj = surveyRepository.findAllPointsWithVotes(ids, userId);
        return surveyMapper.toPageDto(surveys, votingEstimatesProj);
    }

    public void createSurvey(SurveyCreateDTO dto, Long userId){
        if(!moderatorRepository.existsByUserIdAndProjectId(userId, dto.getProjectId())){
            throw new ModeratorAccessDeniedException(userId);
        }
        surveyRepository.save(surveyMapper.toEntity(dto, userId, dto.getProjectId()));
    }

    public void deleteSurvey(Long surveyId, Long userId){
        SurveyEntity survey = surveyRepository.findSurveyById(surveyId)
                .orElseThrow(() -> new SurveyNotFoundException(surveyId));
        ModeratorEntity moderator = moderatorRepository.findByUserIdAndProjectId(userId, survey.getProjectId())
                .orElseThrow(() -> new ModeratorAccessDeniedException(userId));
        RoleOfProjectManager role = moderator.getRole();
        if (role.equals(RoleOfProjectManager.OWNER) || role.equals(RoleOfProjectManager.MANAGER)) {
            throw new SurveyAccessDeniedException(userId);
        }
        surveyRepository.delete(survey);
    }

    public void doVote(Long votingPointId, Long userId){
        VotingPointEntity votingPoint = votingPointRepository.findById(votingPointId)
                .orElseThrow(() -> new VotingPointNotFoundException(votingPointId));
        SurveyEntity survey = votingPoint.getSurvey();

        timeUpCheck(survey);
        if (isRadioButtonType(survey)){
            radioButtonVote(survey.getId(), userId, votingPointId);
        } else {
            checkBoxVote(userId, votingPointId);
        }

    }

    private void radioButtonVote(Long surveyId, Long userId, Long votingPointId){
        PointEstimateEntity pointEstimate = pointEstimateRepository.findByUserIdAndSurveyId(userId, surveyId)
                .orElse(null);
        if (pointEstimate == null) {
            createPointEstimate(userId, votingPointId);
        } else {
            if (pointEstimate.getVotingPoint().getId().equals(votingPointId)) {
                pointEstimateRepository.delete(pointEstimate);
            } else {
                pointEstimateRepository.delete(pointEstimate);
                createPointEstimate(userId, votingPointId);
            }
        }
    }

    private void checkBoxVote(Long userId, Long votingPointId){
        PointEstimateEntity pointEstimate = pointEstimateRepository.findByUserIdAndVotingPointId(userId, votingPointId)
                .orElse(null);
        if (pointEstimate == null) {
            createPointEstimate(userId, votingPointId);
        } else {
            pointEstimateRepository.delete(pointEstimate);
        }
    }

    private void createPointEstimate(Long userId, Long votingPointId){
        pointEstimateRepository.save(pointEstimateMapper.toEntity(
                userRepository.getReferenceById(userId),
                votingPointRepository.getReferenceById(votingPointId)));
    }

    private void timeUpCheck(SurveyEntity survey){
        if (survey.getEndDate().isBefore(LocalDateTime.now())){
            throw new SurveyVotingTimeIsUpException();
        }
    }

    private boolean isRadioButtonType(SurveyEntity survey){
        return survey.getType().equals(TypeOfSurveyEnum.RADIO_BUTTON);
    }

}
