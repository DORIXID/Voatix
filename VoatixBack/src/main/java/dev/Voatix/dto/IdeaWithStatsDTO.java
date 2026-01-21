package dev.Voatix.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
