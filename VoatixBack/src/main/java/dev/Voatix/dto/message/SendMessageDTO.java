package dev.Voatix.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageDTO {
    private String text;
    private Long receiverId;
    private LocalDateTime date;
    private List<Long> files;
}
