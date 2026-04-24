package dev.Voatix.dto.idea;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdeaSingleRequestDTO {

    @NotNull
    public Long ideaId;
}
