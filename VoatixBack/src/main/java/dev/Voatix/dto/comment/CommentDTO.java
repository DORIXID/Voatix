package dev.Voatix.dto.comment;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentDTO {
    private Long id;
    private String text;
    private Long userId;
    private String username;
    private Long avatarId;
    private Long ideaId;
    private LocalDateTime dateTime;
    @Size(max = 5)
    private List<Long> fileIds = new ArrayList<>();
}
