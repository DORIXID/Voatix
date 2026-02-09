package dev.Voatix.utils.exceptions.fileException;

public class FileProcessingException extends RuntimeException {
    public FileProcessingException(String filename) {
        super("Failed to process file: " + filename);
    }
}

