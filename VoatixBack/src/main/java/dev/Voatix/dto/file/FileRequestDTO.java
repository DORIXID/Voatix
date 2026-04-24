package dev.Voatix.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileRequestDTO {
    private Long id;
    private String name;
    private String contentType;
    private String bucket;
    private Long commentId;
    private Long messageId;
    private Long ideaId;
}

