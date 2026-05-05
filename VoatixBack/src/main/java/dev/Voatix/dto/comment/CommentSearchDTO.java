package dev.Voatix.dto.comment;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentSearchDTO {
    @NotNull
    Long ideaId;
    Integer page = 0;
    @Min(1)
    @Max(30)
    Integer limit = 12;
}
