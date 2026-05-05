package dev.Voatix.dto.idea;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdeaLikeDTO {
    Long ideaId;
    @Min(-1)
    @Max(1)
    Integer like;
}
