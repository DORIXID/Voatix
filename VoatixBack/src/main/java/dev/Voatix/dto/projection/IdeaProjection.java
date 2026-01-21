package dev.Voatix.dto.projection;

import dev.Voatix.entity.enums.IdeaStatusEnum;
import jakarta.validation.constraints.Size;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public interface IdeaProjection {
    Long getId();
    String getTitle();
    String getDescription();
    LocalDateTime getDateTime();
    Long getProjectId();
    Long getUserId();
    IdeaStatusEnum getStatus();
}
