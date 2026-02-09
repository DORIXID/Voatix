package dev.Voatix.utils.exceptions.commentException;

public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(Long id) {
        super("Comment " + id + " not found");
    }
}
