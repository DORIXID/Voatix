package dev.Voatix.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {

    private String text;
    private String sender;
    private String receiver;
    private Boolean isRead;
    private LocalDateTime date;
    private List<String> files;
}
