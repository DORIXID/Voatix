package dev.Voatix.utils.exceptions.ideaException;

public class VoteStatsNotFoundException extends RuntimeException {
    public VoteStatsNotFoundException(Long ideaId) {
        super("VoteStats for ideaId " + ideaId + " not found");
    }
}

