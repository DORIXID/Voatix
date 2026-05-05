package dev.Voatix.utils.exceptions.userException;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String nickname) {
        super("User with nickname \"" + nickname + "\" already exists");
    }
}

