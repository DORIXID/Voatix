package dev.Voatix.utils.exceptions.commonException;

public class UnknownStatusException extends RuntimeException {
    public UnknownStatusException(String status) {
        super("Unknown status: " + status);
    }
}
