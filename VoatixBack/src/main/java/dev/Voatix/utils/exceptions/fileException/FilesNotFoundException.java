package dev.Voatix.utils.exceptions.fileException;

public class FilesNotFoundException extends RuntimeException {
    public FilesNotFoundException() {
        super("Files are not found");
    }
}
