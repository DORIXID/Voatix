package dev.Voatix.utils.exceptions.surveyException;

public class SurveyVotingTimeIsUpException extends RuntimeException {
    public SurveyVotingTimeIsUpException() {
        super("Voting time is up");
    }
}

