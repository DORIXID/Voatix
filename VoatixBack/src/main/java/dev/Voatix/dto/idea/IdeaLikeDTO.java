package dev.Voatix.dto.idea;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdeaLikeDTO {
    Long ideaId;
    @Size(min = -1, max = 1)
    Integer like;
}
