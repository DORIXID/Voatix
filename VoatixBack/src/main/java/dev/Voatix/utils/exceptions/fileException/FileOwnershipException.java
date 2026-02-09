package dev.Voatix.utils.exceptions.fileException;

public class FileOwnershipException extends RuntimeException {
    public FileOwnershipException() {
        super("Access denied: Some files don't belong to you");
    }
}

