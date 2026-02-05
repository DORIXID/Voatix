package dev.Voatix.controllers;


import dev.Voatix.dto.SurveyCreateDTO;
import dev.Voatix.dto.SurveyResponseDTO;
import dev.Voatix.service.SurveyService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("api/surveys")
public class SurveysController {

    @Autowired
    private SurveyService surveyService;

    @GetMapping("")
    public Page<SurveyResponseDTO> getSurveys(
            @RequestParam(required = true) String project,
            @RequestParam() Integer page,
            @RequestParam(defaultValue = "1") Integer limit,
            @RequestParam(defaultValue = "", required = false) String filterBy,
            @RequestParam(defaultValue = "", required = false) String searchedValue,
            Principal principal) {
        return surveyService.getSurveys(
                project,
                page,
                limit,
                filterBy,
                searchedValue,
                principal);
    }

    @PostMapping("new")
    public ResponseEntity<Void> createSurvey(
            @Valid @RequestBody SurveyCreateDTO dto,
            Principal principal
    ){
        surveyService.createSurvey(dto, principal);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{surveyId}")
    public ResponseEntity<Void> deleteSurvey(
            @PathVariable("surveyId") Long surveyId,
            Principal principal
    ){
        surveyService.deleteSurvey(surveyId, principal);
        return ResponseEntity.ok().build();
    }

    @PostMapping("vote/{votingPointId}")
    public ResponseEntity<Void> voteSurvey(
            @PathVariable("votingPointId") Long votingPointId,
            Principal principal
    ){
        surveyService.doVote(votingPointId, principal);
        return ResponseEntity.ok().build();
    }


}
