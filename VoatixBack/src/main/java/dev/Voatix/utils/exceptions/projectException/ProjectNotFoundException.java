package dev.Voatix.utils.exceptions.projectException;

public class ProjectNotFoundException extends RuntimeException {
    public ProjectNotFoundException(String title) {
        super("Project \"" + title + "\" not found");
    }
}
