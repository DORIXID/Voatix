package dev.Voatix.utils.exceptions.moderatorException;

public class ModeratorAccessDeniedException extends RuntimeException {
    public ModeratorAccessDeniedException(Long id) {
        super("User \"" + id + "\" does not have access");
    }
}
