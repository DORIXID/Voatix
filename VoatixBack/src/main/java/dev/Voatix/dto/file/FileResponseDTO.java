package dev.Voatix.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileResponseDTO{
    private Long id;
    private String name;
    private String contentType;
    private String bucket;
}
