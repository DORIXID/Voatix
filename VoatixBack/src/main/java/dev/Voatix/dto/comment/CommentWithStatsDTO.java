package dev.Voatix.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentWithStatsDTO {
    private CommentDTO comment;
    private Long likes;
    private Long disLikes;
    private Long vote;
}
