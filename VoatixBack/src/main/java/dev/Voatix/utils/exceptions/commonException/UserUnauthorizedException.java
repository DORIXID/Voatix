package dev.Voatix.utils.exceptions.commonException;

public class UserUnauthorizedException extends RuntimeException {
    public UserUnauthorizedException(Long userId) {
        super("User \"" + userId + "\" not found");
    }
}
