package dev.Voatix.dto.survey;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SurveyVoteDTO {
    @NotNull
    public Long votingPointId;
}
