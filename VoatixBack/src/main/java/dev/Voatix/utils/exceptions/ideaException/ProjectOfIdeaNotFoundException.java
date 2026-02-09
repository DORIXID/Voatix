package dev.Voatix.utils.exceptions.ideaException;

public class ProjectOfIdeaNotFoundException extends RuntimeException {
    public ProjectOfIdeaNotFoundException(Long ideaId) {
        super("Project of idea \"" + ideaId + "\" not found");
    }
}
