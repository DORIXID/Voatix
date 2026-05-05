package dev.Voatix.dto.project;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.Voatix.entity.enums.RoleOfProjectManager;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectOfUserDTO {
    private Long id;
    @Size(min = 3, max = 30)
    private String title;
    private Long fileId;
    private RoleOfProjectManager roleOfUser;
}
