package dev.Voatix.dto.survey;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.Voatix.entity.enums.TypeOfSurveyEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SurveyResponseDTO {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private TypeOfSurveyEnum type;
    private String creatorName;
    private List<VotingEstimatesDTO> votingEstimates;
}
