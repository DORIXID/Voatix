package dev.Voatix.dto;

import dev.Voatix.entity.enums.TypeOfSurveyEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SurveyCreateDTO {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private TypeOfSurveyEnum type;
    private String projectName;
    private List<VotingPointDTO> votingPoints;
}
