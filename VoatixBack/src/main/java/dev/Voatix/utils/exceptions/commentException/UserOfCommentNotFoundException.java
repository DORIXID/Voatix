package dev.Voatix.utils.exceptions.commentException;

public class UserOfCommentNotFoundException extends RuntimeException {
    public UserOfCommentNotFoundException(Long commentId) {
        super("User of comment \"" + commentId + "\" not found");
    }
}
