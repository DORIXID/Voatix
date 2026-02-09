package dev.Voatix.utils.exceptions.surveyException;

public class SurveyAccessDeniedException extends RuntimeException {
    public SurveyAccessDeniedException(String nickname) {
        super("User \"" + nickname + "\" does not have access to delete this survey");
    }
}
