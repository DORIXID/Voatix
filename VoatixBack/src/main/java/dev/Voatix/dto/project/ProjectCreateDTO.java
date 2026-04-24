package dev.Voatix.dto.project;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectCreateDTO {
    @Size(min = 3, max = 30)
    private String title;
    private Long fileId;
}
