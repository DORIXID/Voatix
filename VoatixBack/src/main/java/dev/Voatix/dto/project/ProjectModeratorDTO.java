package dev.Voatix.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectModeratorDTO {
    @NotNull
    private Long projectId;
    @NotNull
    private Long moderatorId;
}
