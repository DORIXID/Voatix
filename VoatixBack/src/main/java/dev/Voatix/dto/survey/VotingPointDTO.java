package dev.Voatix.dto.survey;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VotingPointDTO {
    private Long id;
    @Size(min = 1, max = 50)
    private String title;
}
