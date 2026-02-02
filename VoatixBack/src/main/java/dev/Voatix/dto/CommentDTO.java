package dev.Voatix.dto;

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
    private Long userId;
    private Long ideaId;
    private LocalDateTime dateTime;
    private List<String> fileKeys = new ArrayList<>();
}
