package dev.Voatix.mapper;


import dev.Voatix.dto.survey.VotingPointDTO;
import dev.Voatix.entity.VotingPointEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VotingPointMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "survey", ignore = true)
    @Mapping(target = "pointEstimates", ignore = true)
    VotingPointEntity toEntity(VotingPointDTO dto);
}
