package dev.Voatix.utils.exceptions.surveyException;

public class SurveyNotFoundException extends RuntimeException {
    public SurveyNotFoundException(Long id) {
        super("Survey " + id + " not found");
    }
}

