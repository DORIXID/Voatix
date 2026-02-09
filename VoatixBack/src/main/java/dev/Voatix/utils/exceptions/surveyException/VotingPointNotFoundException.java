package dev.Voatix.utils.exceptions.surveyException;

public class VotingPointNotFoundException extends RuntimeException {
    public VotingPointNotFoundException(Long id) {
        super("Voting point " + id + " not found");
    }
}

