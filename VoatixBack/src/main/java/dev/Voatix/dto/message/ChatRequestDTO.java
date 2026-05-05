package dev.Voatix.dto.message;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequestDTO {
    @NotNull
    Long companionId;
    @Min(0)
    @NotNull
    Integer page = 0;
    @Min(1)
    @Max(100)
    @NotNull
    Integer limit = 12;
}
