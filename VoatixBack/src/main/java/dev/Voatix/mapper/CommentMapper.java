package dev.Voatix.mapper;


import dev.Voatix.dto.CommentDTO;
import dev.Voatix.dto.CommentUpdateDTO;
import dev.Voatix.dto.CommentWithStatsDTO;
import dev.Voatix.dto.IdeaUpdateDTO;
import dev.Voatix.dto.projection.*;
import dev.Voatix.entity.*;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    default Page<CommentWithStatsDTO> toPageDto(
            Page<CommentEntity> commentPage,
            List<CommentStatsProjection> votes
    ) {

        Map<Long, CommentStatsProjection> voteMap = votes.stream()
                .collect(Collectors.toMap(CommentStatsProjection::getCommentId, c -> c));

        return commentPage.map(comm -> toStatsDto(
                comm,
                voteMap.get(comm.getId())
        ));
    }

    @Mapping(target = "comment", source = "comment")
    @Mapping(target = "likes", expression = "java(stats != null ? stats.getLikes() : 0L)")
    @Mapping(target = "disLikes", expression = "java(stats != null ? stats.getDislikes() : 0L)")
    @Mapping(target = "vote", expression = "java(stats != null ? stats.getUserVote() : 0L)")
    CommentWithStatsDTO toStatsDto(
            CommentEntity comment,
            CommentStatsProjection stats
    );

    @Mapping(target = "userId", source = "comment.user.id")
    @Mapping(target = "ideaId", source = "comment.idea.id")
    @Mapping(target = "fileKeys", source = "comment.files")
    CommentDTO toCommentDto(CommentEntity comment);

    default String map(FileEntity file) {
        if (file == null) {
            return null;
        }
        return file.getKey();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "text", source = "dto.text")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "idea", source = "idea")
    @Mapping(target = "files", source = "files")
    @Mapping(target = "dateTime", expression = "java(java.time.LocalDateTime.now())")
    CommentEntity toEntity(CommentDTO dto, UserEntity user, IdeaEntity idea, List<FileEntity> files);

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(CommentUpdateDTO dto, @MappingTarget CommentEntity entity);

}
