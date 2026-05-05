package dev.Voatix.utils.exceptions.userException;

public class UserNameAlreadyExistsException extends RuntimeException {
    public UserNameAlreadyExistsException(String nickname) {
        super("User with nickname \"" + nickname + "\" already exists");
    }
}

