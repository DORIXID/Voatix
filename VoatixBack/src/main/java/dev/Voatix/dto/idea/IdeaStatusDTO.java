package dev.Voatix.dto.idea;

import dev.Voatix.entity.enums.IdeaStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdeaStatusDTO {
    @NotNull
    private Long ideaId;
    @NotNull(message = "status cannot be null")
    private IdeaStatusEnum status;
}
