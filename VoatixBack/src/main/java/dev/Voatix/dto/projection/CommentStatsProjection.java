package dev.Voatix.dto.projection;

public interface CommentStatsProjection {
    Long getCommentId();
    Long getLikes();
    Long getDislikes();
    Long getUserVote();
}
