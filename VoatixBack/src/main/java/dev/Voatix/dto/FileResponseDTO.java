package dev.Voatix.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileResponseDTO{
    private String name;
    private String contentType;
    private String bucket;
    private String key;
    private String url;
}
