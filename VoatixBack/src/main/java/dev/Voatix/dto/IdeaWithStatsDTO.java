package dev.Voatix.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IdeaWithStatsDTO {
    private IdeaDTO idea;
    private Long likes;
    private Long disLikes;

    public IdeaWithStatsDTO(IdeaDTO idea, Long likes, Long disLikes) {
        this.idea = idea;
        this.likes = likes;
        this.disLikes = disLikes;
    }

    public IdeaWithStatsDTO() {
    }
}
