package dev.Voatix.controllers;

import dev.Voatix.utils.exceptions.InvalidCredentialsException;
import dev.Voatix.utils.exceptions.UserDisabledException;
import dev.Voatix.utils.exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(value = UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFoundException(UserNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(value = UserDisabledException.class)
    public ResponseEntity<?> handleDisabledException(UserDisabledException ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(value = InvalidCredentialsException.class)
    public ResponseEntity<?> handleInvalidCredentialsException(InvalidCredentialsException ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", ex.getMessage()));
    }
}
