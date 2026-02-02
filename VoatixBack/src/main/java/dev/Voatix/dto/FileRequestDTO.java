package dev.Voatix.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileRequestDTO {
    private String name;
    private String key;
    private String contentType;
    private String bucket;
    private Long comment_id;
    private Long message_id;
    private Long idea_id;
}

