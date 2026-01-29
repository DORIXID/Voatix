package dev.Voatix.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)

public class IdeaUpdateDTO {
    @Size(min = 10, max = 50)
    private String title;
    @Size(min = 30, max = 1600)
    private String description;
}