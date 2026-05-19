package dev.Voatix.dto.message;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageDTO {
    private String text;
    @NotNull
    private Long receiverId;
    private LocalDateTime date;
    private List<Long> files;
}
