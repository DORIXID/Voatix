package dev.Voatix.mapper;

import dev.Voatix.entity.PointEstimateEntity;
import dev.Voatix.entity.UserEntity;
import dev.Voatix.entity.VotingPointEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PointEstimateMapper {

    @Mapping(target = "user", source = "user")
    @Mapping(target = "votingPoint", source = "votingPoint")
    @Mapping(target = "id.userId", source = "user.id")
    @Mapping(target = "id.pointId", source = "votingPoint.id")
    PointEstimateEntity toEntity(UserEntity user, VotingPointEntity votingPoint);
}
