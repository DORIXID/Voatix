package dev.Voatix.utils.exceptions.surveyException;

public class SurveyAccessDeniedException extends RuntimeException {
    public SurveyAccessDeniedException(Long userId) {
        super("User \"" + userId + "\" does not have access to delete this survey");
    }
}
