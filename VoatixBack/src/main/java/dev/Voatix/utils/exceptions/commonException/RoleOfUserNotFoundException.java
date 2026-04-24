package dev.Voatix.utils.exceptions.commonException;

public class RoleOfUserNotFoundException extends RuntimeException {
    public RoleOfUserNotFoundException(Long userId) {
        super("Role of user \"" + userId + "\" not found");
    }
}
