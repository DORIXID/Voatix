package dev.Voatix.dto.idea;

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
    private String nickname;
    private Long avatarId;
    private IdeaStatusEnum status;
    @Size(max = 6)
    private List<Long> fileIds;
}
