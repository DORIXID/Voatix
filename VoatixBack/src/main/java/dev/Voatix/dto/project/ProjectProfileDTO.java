package dev.Voatix.dto.project;

import dev.Voatix.dto.user.UserModeratorProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectProfileDTO {
    private String title;
    private Long fileId;
    private List<UserModeratorProjection> moderators;
}
