package dev.Voatix.dto.message;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(min = 1, max = 100)
    Integer limit = 12;
}
