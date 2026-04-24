package dev.Voatix.mapper;

import dev.Voatix.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface VotingEstimateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "idea", source = "idea")
    @Mapping(target = "id.userId", source = "userId")
    @Mapping(target = "id.ideaId", source = "ideaId")
    @Mapping(target = "isLike", source = "like")
    VotingEstimateEntity toEntity(UserEntity user, IdeaEntity idea,  Long userId, Long ideaId, Boolean like);

    @Mapping(target = "isLike", source = "like")
    void updateEntity(@MappingTarget VotingEstimateEntity entity, Boolean like);
}
