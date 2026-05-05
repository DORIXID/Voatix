package dev.Voatix.dto.idea;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdeaSearchDTO {
    @NotNull
    Long projectId;
    Integer page = 0;
    @Min(1)
    @Max(50)
    Integer limit = 12;
    String filterBy = "";
    String search = "";

}
