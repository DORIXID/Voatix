package dev.Voatix.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class IdeaWithStatsDTO {
    private IdeaDTO idea;
    private Long likes;
    private Long disLikes;
    private Long vote;
    private Long commentsCount;
    public IdeaWithStatsDTO(IdeaDTO idea, Long likes, Long disLikes, Long vote, Long commentsCount) {
        this.idea = idea;
        this.likes = likes;
        this.disLikes = disLikes;
        this.vote = vote;
        this.commentsCount = commentsCount;
    }

    public IdeaWithStatsDTO() {
    }
}
