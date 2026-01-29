package dev.Voatix.dto;

import dev.Voatix.entity.enums.IdeaStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdeaStatusDTO {
    @NotNull(message = "status cannot be null")
    private IdeaStatusEnum status;
}
