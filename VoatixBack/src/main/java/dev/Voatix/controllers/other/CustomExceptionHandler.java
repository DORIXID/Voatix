package dev.Voatix.controllers.other;

import dev.Voatix.utils.exceptions.commentException.CommentAuthorNotFoundException;
import dev.Voatix.utils.exceptions.commentException.CommentNotFoundException;
import dev.Voatix.utils.exceptions.commentException.UserOfCommentNotFoundException;
import dev.Voatix.utils.exceptions.commonException.AccessDeniedException;
import dev.Voatix.utils.exceptions.commonException.RoleOfUserNotFoundException;
import dev.Voatix.utils.exceptions.commonException.UnknownStatusException;
import dev.Voatix.utils.exceptions.commonException.UserUnauthorizedException;
import dev.Voatix.utils.exceptions.fileException.*;
import dev.Voatix.utils.exceptions.ideaException.*;
import dev.Voatix.utils.exceptions.moderatorException.ModeratorAccessDeniedException;
import dev.Voatix.utils.exceptions.projectException.ProjectNotFoundException;
import dev.Voatix.utils.exceptions.surveyException.SurveyAccessDeniedException;
import dev.Voatix.utils.exceptions.surveyException.SurveyNotFoundException;
import dev.Voatix.utils.exceptions.surveyException.SurveyVotingTimeIsUpException;
import dev.Voatix.utils.exceptions.surveyException.VotingPointNotFoundException;
import dev.Voatix.utils.exceptions.userException.UserAlreadyExistsException;
import dev.Voatix.utils.exceptions.userException.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class CustomExceptionHandler {

    // 400 BAD REQUEST
    @ExceptionHandler({
            UnknownStatusException.class,
            InvalidFileTypeException.class,
            FileProcessingException.class,
            FilesNotFoundException.class,
            UserAlreadyExistsException.class
    })
    public ResponseEntity<?> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    // 401 UNAUTHORIZED
    @ExceptionHandler(UserUnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorized(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", ex.getMessage()));
    }

    // 403 FORBIDDEN
    @ExceptionHandler({
            AccessDeniedException.class,
            IdeaAccessDeniedException.class,
            FileOwnershipException.class,
            ModeratorAccessDeniedException.class,
            SurveyAccessDeniedException.class
    })
    public ResponseEntity<?> handleForbidden(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage()));
    }

    // 404 NOT FOUND
    @ExceptionHandler({
            ProjectOfIdeaNotFoundException.class,
            RoleOfUserNotFoundException.class,
            CommentNotFoundException.class,
            CommentAuthorNotFoundException.class,
            UserOfCommentNotFoundException.class,
            IdeaNotFoundException.class,
            VoteStatsNotFoundException.class,
            CommentCountNotFoundException.class,
            FileNotFoundException.class,
            ProjectNotFoundException.class,
            SurveyNotFoundException.class,
            VotingPointNotFoundException.class,
            UserNotFoundException.class
    })
    public ResponseEntity<?> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    // 410 GONE
    @ExceptionHandler(SurveyVotingTimeIsUpException.class)
    public ResponseEntity<?> handleGone(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.GONE)
                .body(Map.of("error", ex.getMessage()));
    }
}
