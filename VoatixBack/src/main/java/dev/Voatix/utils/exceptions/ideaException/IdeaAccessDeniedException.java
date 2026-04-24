package dev.Voatix.utils.exceptions.ideaException;

public class IdeaAccessDeniedException extends RuntimeException {
    public IdeaAccessDeniedException(Long id) {
        super("User \"" + id + "\" does not have access to this idea");
    }
}

