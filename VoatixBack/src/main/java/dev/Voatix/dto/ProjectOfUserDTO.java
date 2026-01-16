package dev.Voatix.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.Voatix.entity.enums.RoleOfProjectManager;
import jakarta.validation.constraints.Size;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectOfUserDTO {
    private Long projectId;
    @Size(min = 3, max = 30)
    private String title;
    private Long avatar_id;
    private Boolean active;
    private RoleOfProjectManager roleOfUser;
}
