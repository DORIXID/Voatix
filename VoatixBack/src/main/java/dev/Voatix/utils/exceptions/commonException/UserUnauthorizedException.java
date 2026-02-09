package dev.Voatix.utils.exceptions.commonException;

public class UserUnauthorizedException extends RuntimeException {
    public UserUnauthorizedException(String nickname) {
        super("User \"" + nickname + "\" not found");
    }
}
