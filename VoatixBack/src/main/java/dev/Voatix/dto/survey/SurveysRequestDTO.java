package dev.Voatix.dto.survey;

import jakarta.validation.constraints.*;
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
    @Min(1)
    @Max(10)
    private Integer limit = 1;
    private String filterBy = "";
    private String searchedValue = "";

}
