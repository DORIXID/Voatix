package dev.Voatix.mapper;

import dev.Voatix.dto.SurveyCreateDTO;
import dev.Voatix.dto.SurveyResponseDTO;
import dev.Voatix.dto.VotingEstimatesDTO;
import dev.Voatix.dto.projection.VotingEstimatesProjection;
import dev.Voatix.entity.ProjectEntity;
import dev.Voatix.entity.SurveyEntity;
import dev.Voatix.entity.UserEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {VotingPointMapper.class})
public interface SurveyMapper {

    default Page<SurveyResponseDTO> toPageDto(
            Page<SurveyEntity> surveyPage,
            List<VotingEstimatesProjection> votes
    ) {

        Map<Long, List<VotingEstimatesProjection>> voteMap = votes.stream()
                .collect(Collectors.groupingBy(VotingEstimatesProjection::getSurveyId));

        return surveyPage.map(survey -> toStatsDto(
                survey,
                voteMap.getOrDefault(survey.getId(), Collections.emptyList()))
        );
    }

    @Mapping(target = "votingEstimates", source = "votingEstimates")
    @Mapping(target = "id", source = "survey.id")
    @Mapping(target = "title", source = "survey.title")
    @Mapping(target = "description", source = "survey.description")
    @Mapping(target = "startDate", source = "survey.startDate")
    @Mapping(target = "endDate", source = "survey.endDate")
    @Mapping(target = "type", source = "survey.type")
    @Mapping(target = "creatorName", source = "survey.user.nickname")
    SurveyResponseDTO toStatsDto(
            SurveyEntity survey,
            List<VotingEstimatesProjection> votingEstimates
    );

    VotingEstimatesDTO toDto(VotingEstimatesProjection proj);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "title", source = "dto.title")
    @Mapping(target = "description", source = "dto.description")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "project", source = "project")
    @Mapping(target = "votingPoints", source = "dto.votingPoints")
    SurveyEntity toEntity(SurveyCreateDTO dto, UserEntity user, ProjectEntity project);

    @AfterMapping
    default void afterMapping(@MappingTarget SurveyEntity survey){
        survey.getVotingPoints().forEach(votingPoint -> votingPoint.setSurvey(survey));
    }
}
