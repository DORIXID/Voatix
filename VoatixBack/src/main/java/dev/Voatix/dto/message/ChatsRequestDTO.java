package dev.Voatix.dto.message;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatsRequestDTO {
    @Min(0)
    @NotNull
    Integer page = 0;
    @Min(1)
    @Max(100)
    Integer limit = 12;
}
