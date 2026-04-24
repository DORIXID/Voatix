package dev.Voatix.controllers;


import dev.Voatix.configuration.security.CustomUserDetails;
import dev.Voatix.dto.survey.SurveyCreateDTO;
import dev.Voatix.dto.survey.SurveyDeleteDTO;
import dev.Voatix.dto.survey.SurveyResponseDTO;
import dev.Voatix.dto.survey.SurveysRequestDTO;
import dev.Voatix.service.SurveyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/surveys")
public class SurveysController {

    final private SurveyService surveyService;

    @GetMapping("")
    public Page<SurveyResponseDTO> getSurveys(
            @Valid @RequestBody SurveysRequestDTO dto,
            Authentication auth) {
        return surveyService.getSurveys(
                dto,
                getUserId(auth));
    }

    @PostMapping("new")
    public void createSurvey(
            @Valid @RequestBody SurveyCreateDTO dto,
            Authentication auth
    ){
        surveyService.createSurvey(dto, getUserId(auth));
    }

    @DeleteMapping("")
    public void deleteSurvey(
            @Valid @RequestBody SurveyDeleteDTO dto,
            Authentication auth
    ){
        surveyService.deleteSurvey(dto.getSurveyId(), getUserId(auth));
    }

    @PostMapping("vote")
    public void voteSurvey(
            @Valid @RequestBody SurveyResponseDTO dto,
            Authentication auth
    ){
        surveyService.doVote(dto.getId(), getUserId(auth));
    }

    Long getUserId(Authentication auth) {
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        return user.getId();
    }
}
