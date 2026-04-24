package dev.Voatix.dto.comment;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentLikeDTO {
    Long comentId;
    @Size(min = -1, max = 1)
    Integer like;
}
