package dev.Voatix.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.Voatix.entity.enums.IdeaStatusEnum;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdeaDTO {
    private Long id;
    @Size(min = 10, max = 50)
    private String title;
    @Size(min = 30, max = 1600)
    private String description;
    private LocalDateTime dateTime;
    private Long projectId;
    private Long userId;
    private Long userAvatarId;
    private IdeaStatusEnum status;
    private List<String> fileKeys;
}
