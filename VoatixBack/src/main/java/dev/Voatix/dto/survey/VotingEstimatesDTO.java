package dev.Voatix.dto.survey;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VotingEstimatesDTO {
    private Long id;
    private String title;
    private Long votesCount;
    private Boolean isVoted;
}
