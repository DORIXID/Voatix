package dev.Voatix.mapper;


import dev.Voatix.dto.CommentDTO;
import dev.Voatix.dto.CommentUpdateDTO;
import dev.Voatix.dto.CommentWithStatsDTO;
import dev.Voatix.dto.IdeaUpdateDTO;
import dev.Voatix.dto.projection.*;
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
            List<CommentFileKeyProjection> files
    ) {

        Map<Long, CommentStatsProjection> votesMap = votes.stream()
                .collect(Collectors.toMap(CommentStatsProjection::getCommentId, v -> v, (a, b) -> a));

        Map<Long, List<String>> filesMap = files.stream()
                .filter(f -> f.getCommentId() != null)
                .collect(Collectors.groupingBy(
                        CommentFileKeyProjection::getCommentId,
                        Collectors.mapping(CommentFileKeyProjection::getKey, Collectors.toList())
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
    CommentWithStatsDTO toStatsDto(CommentEntity comment, CommentStatsProjection stats, @Context List<String> fileKeys);

    @Mapping(target = "username", source = "comment.user.nickname")
    @Mapping(target = "avatarKey", source = "comment.user.avatar.key")
    @Mapping(target = "ideaId", source = "comment.idea.id")
    @Mapping(target = "fileKeys", ignore = true)
    CommentDTO toCommentDto(CommentEntity comment);

    @AfterMapping
    default void linkFileKeys(@MappingTarget CommentWithStatsDTO dto, @Context List<String> fileKeys) {
        if (dto.getComment() != null && fileKeys != null) {
            dto.getComment().setFileKeys(fileKeys);
        }
    }

    default String map(FileEntity file) {
        if (file == null) {
            return null;
        }
        return file.getKey();
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
