package dev.Voatix.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.Voatix.entity.enums.IdeaStatusEnum;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.sql.Timestamp;
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IdeaDTO {
    private Long id;
    @Size(min = 10, max = 50)
    private String title;
    @Size(min = 30, max = 1600)
    private String description;
    private Timestamp dateTime;
    private Long project_id;
    private Long user_id;
    private IdeaStatusEnum status;
}
