package dev.Voatix.dto.projection;

public interface VoteStatsProjection {
    Long getIdeaId();
    Long getLikes();
    Long getDislikes();
    Long getUserVote();
}
