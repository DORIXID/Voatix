package dev.Voatix.utils.exceptions.fileException;

public class InvalidFileTypeException extends RuntimeException {
    public InvalidFileTypeException(String contentType) {
        super("Invalid file type: " + contentType + ". Only PNG and JPEG are allowed");
    }
}
