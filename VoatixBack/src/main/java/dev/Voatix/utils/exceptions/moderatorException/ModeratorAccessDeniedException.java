package dev.Voatix.utils.exceptions.moderatorException;

public class ModeratorAccessDeniedException extends RuntimeException {
    public ModeratorAccessDeniedException(String nickname) {
        super("User \"" + nickname + "\" does not have access");
    }
}
