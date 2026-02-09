package dev.Voatix.utils.exceptions.ideaException;

public class IdeaNotFoundException extends RuntimeException {
    public IdeaNotFoundException(Long id) {
        super("Idea " + id + " not found");
    }
}
