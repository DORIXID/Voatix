package dev.Voatix.dto;

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
public class CommentDTO {
    private Long id;
    private String text;
    private String username;
    private String avatarKey;
    private Long ideaId;
    private LocalDateTime dateTime;
    @Size(max = 5)
    private List<String> fileKeys = new ArrayList<>();
}
