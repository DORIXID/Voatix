package dev.Voatix.dto;

import dev.Voatix.entity.enums.TypeOfSurveyEnum;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "Заголовок не может быть пустым")
    @Size(max = 100)
    private String title;
    @Size(max = 500)
    private String description;
    @FutureOrPresent
    private LocalDateTime startDate;
    @Future
    private LocalDateTime endDate;
    private TypeOfSurveyEnum type;
    private String projectName;
    @Size(min = 2, max = 20)
    private List<VotingPointDTO> votingPoints;
}
