package dev.Voatix.dto.idea;

public interface VoteStatsProjection {
    Long getIdeaId();
    Long getLikes();
    Long getDislikes();
    Long getUserVote();
}
