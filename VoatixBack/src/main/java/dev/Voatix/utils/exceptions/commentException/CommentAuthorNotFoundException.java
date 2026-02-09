package dev.Voatix.utils.exceptions.commentException;

public class CommentAuthorNotFoundException extends RuntimeException {
    public CommentAuthorNotFoundException(Long commentId) {
        super("Author of comment " + commentId + " not found");
    }
}

