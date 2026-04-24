package dev.Voatix.utils.exceptions.projectException;

public class ProjectNotFoundException extends RuntimeException {
    public ProjectNotFoundException(Long id) {
        super("Project \"" + id + "\" not found");
    }
}
