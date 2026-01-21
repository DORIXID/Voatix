package dev.Voatix.mapper;



import dev.Voatix.dto.IdeaDTO;
import dev.Voatix.dto.IdeaWithStatsDTO;
import dev.Voatix.dto.projection.CommentCountProjection;
import dev.Voatix.dto.projection.IdeaProjection;
import dev.Voatix.dto.projection.VoteStatsProjection;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring")
public interface IdeaMapper {

    public abstract IdeaDTO toDto(IdeaProjection projection);

    @Mapping(target = "idea", source = "ideaProj")
    @Mapping(target = "likes", source = "votes.likes", defaultValue = "0L")
    @Mapping(target = "disLikes", source = "votes.dislikes", defaultValue = "0L")
    @Mapping(target = "vote", source = "votes.userVote", defaultValue = "0L")
    @Mapping(target = "commentsCount", source = "commentProj.count", defaultValue = "0L")
    public abstract IdeaWithStatsDTO toStatsDto(
            IdeaProjection ideaProj,
            VoteStatsProjection votes,
            CommentCountProjection commentProj
    );

    default Page<IdeaWithStatsDTO> toPageDto(
            Page<IdeaProjection> ideaPage,
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
}
