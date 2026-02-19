package dev.Voatix.utils.exceptions.userException;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String nickname) {
        super("user with nickname \"" + nickname + "\" not found");
    }
}
