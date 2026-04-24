package dev.Voatix.mapper;


import dev.Voatix.dto.comment.*;
import dev.Voatix.entity.*;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    default Page<CommentWithStatsDTO> toPageDto(
            Page<CommentEntity> commentPage,
            List<CommentStatsProjection> votes,
            List<CommentFileIdProjection> files
    ) {

        Map<Long, CommentStatsProjection> votesMap = votes.stream()
                .collect(Collectors.toMap(CommentStatsProjection::getCommentId, v -> v, (a, b) -> a));

        Map<Long, List<Long>> filesMap = files.stream()
                .filter(f -> f.getCommentId() != null)
                .collect(Collectors.groupingBy(
                        CommentFileIdProjection::getCommentId,
                        Collectors.mapping(CommentFileIdProjection::getId, Collectors.toList())
                ));

        return commentPage.map(comm -> toStatsDto(
                comm,
                votesMap.get(comm.getId()),
                filesMap.getOrDefault(comm.getId(), Collections.emptyList())
        ));
    }

    @Mapping(target = "comment", source = "comment")
    @Mapping(target = "likes", expression = "java(stats != null ? stats.getLikes() : 0L)")
    @Mapping(target = "disLikes", expression = "java(stats != null ? stats.getDislikes() : 0L)")
    @Mapping(target = "vote", expression = "java(stats != null ? stats.getUserVote() : 0L)")
    CommentWithStatsDTO toStatsDto(CommentEntity comment, CommentStatsProjection stats, @Context List<Long> fileIds);

    @Mapping(target = "username", source = "comment.user.nickname")
    @Mapping(target = "avatarId", source = "comment.user.avatar.id")
    @Mapping(target = "ideaId", source = "comment.idea.id")
    @Mapping(target = "fileIds", ignore = true)
    CommentDTO toCommentDto(CommentEntity comment);

    @AfterMapping
    default void linkFileIds(@MappingTarget CommentWithStatsDTO dto, @Context List<Long> fileIds) {
        if (dto.getComment() != null && fileIds != null) {
            dto.getComment().setFileIds(fileIds);
        }
    }

    default Long map(FileEntity file) {
        if (file == null) {
            return null;
        }
        return file.getId();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "text", source = "dto.text")
    @Mapping(target = "userId", source = "user")
    @Mapping(target = "ideaId", source = "idea")
    @Mapping(target = "files", source = "files")
    @Mapping(target = "dateTime", expression = "java(java.time.LocalDateTime.now())")
    CommentEntity toEntity(CommentDTO dto, Long user, Long idea, List<FileEntity> files);

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(CommentUpdateDTO dto, @MappingTarget CommentEntity entity);

}
