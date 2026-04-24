package dev.Voatix.dto.idea;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(min = 1, max = 30)
    Integer limit = 12;
    String filterBy = "";
    String search = "";

}
