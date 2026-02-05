package dev.Voatix.mapper;

import dev.Voatix.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface VotingEstimateMapper {
    @Mapping(target = "user", source = "user")
    @Mapping(target = "idea", source = "idea")
    @Mapping(target = "id.userId", source = "user.id")
    @Mapping(target = "id.ideaId", source = "idea.id")
    @Mapping(target = "isLike", source = "like")
    VotingEstimateEntity toEntity(UserEntity user, IdeaEntity idea, Boolean like);

    @Mapping(target = "isLike", source = "like")
    void updateEntity(@MappingTarget VotingEstimateEntity entity, Boolean like);
}
