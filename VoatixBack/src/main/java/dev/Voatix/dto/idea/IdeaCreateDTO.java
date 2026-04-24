package dev.Voatix.dto.idea;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IdeaCreateDTO {
    @Size(min = 10, max = 50)
    private String title;
    @Size(min = 30, max = 1600)
    private String description;
    @NotNull
    private Long projectId;
    private List<Long> fileIds= new ArrayList<>();
}