package dev.Voatix.dto.survey;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SurveysRequestDTO {

    @NotNull
    private Long projectId;
    @NotNull
    private Integer page;
    @Size(min = 1, max = 30)
    private Integer limit = 1;
    private String filterBy = "";
    private String searchedValue = "";

}
