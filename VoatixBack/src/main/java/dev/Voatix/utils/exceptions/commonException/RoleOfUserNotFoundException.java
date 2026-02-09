package dev.Voatix.utils.exceptions.commonException;

public class RoleOfUserNotFoundException extends RuntimeException {
    public RoleOfUserNotFoundException(String username) {
        super("Role of user \"" + username + "\" not found");
    }
}
