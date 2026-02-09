package dev.Voatix.mapper;



import dev.Voatix.dto.*;
import dev.Voatix.dto.projection.CommentCountProjection;
import dev.Voatix.dto.projection.IdeaProjection;
import dev.Voatix.dto.projection.VoteStatsProjection;
import dev.Voatix.entity.FileEntity;
import dev.Voatix.entity.IdeaEntity;
import dev.Voatix.entity.ProjectEntity;
import dev.Voatix.entity.UserEntity;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface IdeaMapper {

    default String map(FileEntity file) {
        if (file == null) {
            return null;
        }
        return file.getKey();
    }

    @Mapping(target = "projectId", source = "idea.project.id")
    @Mapping(target = "userId", source = "idea.user.id")
    @Mapping(target = "fileKeys", source = "idea.files")
    @Mapping(target = "avatarKey",
            expression = "java(idea.getUser().getAvatar() != null ? idea.getUser().getAvatar().getKey() : null)")
    IdeaDTO toDto(IdeaEntity idea);

    @Mapping(target = "idea", source = "idea")
    @Mapping(target = "likes", expression = "java(stats != null ? stats.getLikes() : 0L)")
    @Mapping(target = "disLikes", expression = "java(stats != null ? stats.getDislikes() : 0L)")
    @Mapping(target = "vote", expression = "java(stats != null ? stats.getUserVote() : 0L)")
    @Mapping(target = "commentsCount", expression = "java(commentCountProj != null ? commentCountProj.getCount() : 0L)")
    IdeaWithStatsDTO toStatsDto(
            IdeaEntity idea,
            VoteStatsProjection stats,
            CommentCountProjection commentCountProj
    );

    default Page<IdeaWithStatsDTO> toPageDto(
            Page<IdeaEntity> ideaPage,
            List<VoteStatsProjection> votes,
            List<CommentCountProjection> comments) {

        Map<Long, VoteStatsProjection> voteMap = votes.stream()
                .collect(Collectors.toMap(VoteStatsProjection::getIdeaId, v -> v));

        Map<Long, CommentCountProjection> commentMap = comments.stream()
                .collect(Collectors.toMap(CommentCountProjection::getIdeaId, c -> c));

        return ideaPage.map(idea -> toStatsDto(
                idea,
                voteMap.get(idea.getId()),
                commentMap.get(idea.getId())
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
