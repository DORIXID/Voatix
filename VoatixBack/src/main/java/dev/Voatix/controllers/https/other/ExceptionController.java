package dev.Voatix.controllers.https.other;

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
public class ExceptionController {

    //COMMON

    @ExceptionHandler(UserUnauthorizedException.class)
    public ResponseEntity<?> handleUserUnauthorized(UserUnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ProjectOfIdeaNotFoundException.class)
    public ResponseEntity<?> handleProjectOfIdeaNotFoundException(ProjectOfIdeaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(RoleOfUserNotFoundException.class)
    public ResponseEntity<?> handleRoleOfUserNotFoundException(RoleOfUserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    //COMMENT

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<?> handleCommentNotFound(CommentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(CommentAuthorNotFoundException.class)
    public ResponseEntity<?> handleCommentAuthorNotFound(CommentAuthorNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(UserOfCommentNotFoundException.class)
    public ResponseEntity<?> handleUserOfCommentNotFoundException(UserOfCommentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    //IDEA

    @ExceptionHandler(IdeaNotFoundException.class)
    public ResponseEntity<?> handleIdeaNotFound(IdeaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(VoteStatsNotFoundException.class)
    public ResponseEntity<?> handleVoteStatsNotFound(VoteStatsNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(CommentCountNotFoundException.class)
    public ResponseEntity<?> handleCommentCountNotFound(CommentCountNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(UnknownStatusException.class)
    public ResponseEntity<?> handleUnknownStatus(UnknownStatusException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IdeaAccessDeniedException.class)
    public ResponseEntity<?> handleIdeaAccessDenied(IdeaAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage()));
    }

    //FILE

    @ExceptionHandler(FileOwnershipException.class)
    public ResponseEntity<?> handleFileOwnership(FileOwnershipException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<?> handleInvalidFileType(InvalidFileTypeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<?> handleFileProcessing(FileProcessingException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<?> handleFileNotFound(FileNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(FilesNotFoundException.class)
    public ResponseEntity<?> handleFilesNotFound(FilesNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    //MODERATOR

    @ExceptionHandler(ModeratorAccessDeniedException.class)
    public ResponseEntity<?> handleModeratorAccessDenied(ModeratorAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage()));
    }

    //PROJECT

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<?> handleProjectNotFound(ProjectNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    //SURVEY

    @ExceptionHandler(SurveyNotFoundException.class)
    public ResponseEntity<?> handleSurveyNotFound(SurveyNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(SurveyAccessDeniedException.class)
    public ResponseEntity<?> handleSurveyAccessDenied(SurveyAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(VotingPointNotFoundException.class)
    public ResponseEntity<?> handleVotingPointNotFound(VotingPointNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(SurveyVotingTimeIsUpException.class)
    public ResponseEntity<?> handleSurveyVotingTimeIsUp(SurveyVotingTimeIsUpException ex) {
        return ResponseEntity.status(HttpStatus.GONE)
                .body(Map.of("error", ex.getMessage()));
    }

    //USER

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<?> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }
}
