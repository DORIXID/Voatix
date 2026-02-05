package dev.Voatix.mapper;

import dev.Voatix.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CommentRatingMapper {

    @Mapping(target = "user", source = "user")
    @Mapping(target = "comment", source = "comment")
    @Mapping(target = "id.userId", source = "user.id")
    @Mapping(target = "id.commentId", source = "comment.id")
    @Mapping(target = "isLike", source = "like")
    CommentRatingEntity toEntity(UserEntity user, CommentEntity comment, Boolean like);

    @Mapping(target = "isLike", source = "like")
    void updateEntity(@MappingTarget CommentRatingEntity entity, Boolean like);
}
