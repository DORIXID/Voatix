package dev.Voatix.mapper;



import dev.Voatix.dto.idea.*;
import dev.Voatix.dto.comment.CommentCountProjection;
import dev.Voatix.dto.idea.IdeaFileIdProjection;
import dev.Voatix.dto.idea.VoteStatsProjection;
import dev.Voatix.entity.FileEntity;
import dev.Voatix.entity.IdeaEntity;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface IdeaMapper {

    default Long map(FileEntity file) {
        if (file == null) {
            return null;
        }
        return file.getId();
    }

    @Mapping(target = "idea", source = "idea")
    @Mapping(target = "likes", expression = "java(stats != null ? stats.getLikes() : 0L)")
    @Mapping(target = "disLikes", expression = "java(stats != null ? stats.getDislikes() : 0L)")
    @Mapping(target = "vote", expression = "java(stats != null ? stats.getUserVote() : 0L)")
    @Mapping(target = "commentsCount", expression = "java(commentCountProj != null ? commentCountProj.getCount() : 0L)")
    IdeaWithStatsDTO toStatsDto(
            IdeaEntity idea,
            VoteStatsProjection stats,
            CommentCountProjection commentCountProj,
            @Context List<Long> fileIds
    );

    @Mapping(target = "projectId", source = "idea.project.id")
    @Mapping(target = "nickname", source = "idea.user.nickname")
    @Mapping(target = "fileIds", ignore = true)
    @Mapping(target = "avatarId",
            expression = "java(idea.getUser().getAvatar() != null ? idea.getUser().getAvatar().getId() : null)")
    IdeaDTO toDto(IdeaEntity idea);

    @AfterMapping
    default void linkFileIds(@MappingTarget IdeaWithStatsDTO dto, @Context List<Long> fileIds) {
        if (dto.getIdea() != null && fileIds != null) {
            dto.getIdea().setFileIds(fileIds);
        }
    }

    default Page<IdeaWithStatsDTO> toPageDto(
            Page<IdeaEntity> ideaPage,
            List<VoteStatsProjection> votes,
            List<CommentCountProjection> comments,
            List<IdeaFileIdProjection> files) {

        Map<Long, VoteStatsProjection> votesMap = votes.stream()
                .collect(Collectors.toMap(VoteStatsProjection::getIdeaId, v -> v));

        Map<Long, CommentCountProjection> commentsMap = comments.stream()
                .collect(Collectors.toMap(CommentCountProjection::getIdeaId, c -> c));

        Map<Long, List<Long>> filesMap = files.stream()
                .filter(f -> f.getIdeaId() != null)
                .collect(Collectors.groupingBy(
                        IdeaFileIdProjection::getIdeaId,
                        Collectors.mapping(IdeaFileIdProjection::getFileId, Collectors.toList())
                ));

        return ideaPage.map(idea -> toStatsDto(
                idea,
                votesMap.get(idea.getId()),
                commentsMap.get(idea.getId()),
                filesMap.getOrDefault(idea.getId(), Collections.emptyList())
        ));
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "projectId", source = "project")
    @Mapping(target = "userId", source = "user")
    @Mapping(target = "title", source = "dto.title")
    @Mapping(target = "description", source = "dto.description")
    @Mapping(target = "dateTime", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "status", constant = "CREATED")
    @Mapping(target = "files", source = "files")
    IdeaEntity toEntity (IdeaCreateDTO dto, Long project, Long user, List<FileEntity> files);

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(IdeaUpdateDTO dto, @MappingTarget IdeaEntity entity);

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(IdeaStatusDTO dto, @MappingTarget IdeaEntity entity);
}
