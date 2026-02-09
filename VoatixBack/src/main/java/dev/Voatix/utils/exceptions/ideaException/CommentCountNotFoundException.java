package dev.Voatix.utils.exceptions.ideaException;

public class CommentCountNotFoundException extends RuntimeException {
    public CommentCountNotFoundException(Long ideaId) {
        super("CommentCount for ideaId " + ideaId + " not found");
    }
}

