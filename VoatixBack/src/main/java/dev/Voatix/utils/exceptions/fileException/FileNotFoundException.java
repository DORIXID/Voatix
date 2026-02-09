package dev.Voatix.utils.exceptions.fileException;

public class FileNotFoundException extends RuntimeException {
    public FileNotFoundException(String key) {
        super("File with key \"" + key + "\" not found");
    }
}

