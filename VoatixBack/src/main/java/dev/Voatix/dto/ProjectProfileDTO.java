package dev.Voatix.dto;

import dev.Voatix.dto.projection.UserModeratorProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectProfileDTO {
    private String title;
    private String key;
    private List<UserModeratorProjection> moderators;
}
