package dev.Voatix.utils.exceptions.fileException;

public class FileNotFoundException extends RuntimeException {
    public FileNotFoundException(Long id) {
        super("File with key \"" + id + "\" not found");
    }
}

