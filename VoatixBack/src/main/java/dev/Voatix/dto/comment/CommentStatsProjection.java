package dev.Voatix.dto.comment;

public interface CommentStatsProjection {
    Long getCommentId();
    Long getLikes();
    Long getDislikes();
    Long getUserVote();
}
