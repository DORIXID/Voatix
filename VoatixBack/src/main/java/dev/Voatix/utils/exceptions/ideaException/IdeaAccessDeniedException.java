package dev.Voatix.utils.exceptions.ideaException;

public class IdeaAccessDeniedException extends RuntimeException {
    public IdeaAccessDeniedException(String nickname) {
        super("User \"" + nickname + "\" does not have access to this idea");
    }
}

