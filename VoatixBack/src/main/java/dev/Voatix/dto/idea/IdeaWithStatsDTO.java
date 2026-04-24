package dev.Voatix.dto.idea;

import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdeaWithStatsDTO {
    private IdeaDTO idea;
    private Long likes;
    private Long disLikes;
    private Long vote;
    private Long commentsCount;
}
