package com.forthestreets.venueservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        log.warn("Validation Alert: Input constraints violated on path: '{}'", request.getRequestURI());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            if (error instanceof FieldError fieldError) {
                errors.put(fieldError.getField(), fieldError.getDefaultMessage());
            } else {
                errors.put(error.getObjectName(), error.getDefaultMessage());
            }
        });

        Map<String, Object> body = createErrorBody(HttpStatus.BAD_REQUEST, "Input validation failed. Please check the 'errors' map for details.", request);
        body.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("Malformed HTTP Payload: {} | Path requested: '{}'", ex.getMessage(), request.getRequestURI());

        String userFriendlyMessage = "The request payload is malformed or contains invalid data types. " +
                "Verify that numeric values (like latitude and longitude) are sent as actual numbers and not strings.";

        Map<String, Object> body = createErrorBody(HttpStatus.BAD_REQUEST, userFriendlyMessage, request);

        body.put("details", ex.getMostSpecificCause().getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(VenueNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFoundException(
            VenueNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Resource missing alert: {} | Path requested: {}", ex.getMessage(), request.getRequestURI());

        Map<String, Object> body = createErrorBody(HttpStatus.NOT_FOUND, ex.getMessage(), request);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllUncaughtExceptions(
            Exception ex,
            HttpServletRequest request) {

        log.error("CRITICAL: Uncaught server crash on path: {}", request.getRequestURI(), ex);

        Map<String, Object> body = createErrorBody(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected server-side error occurred. The engineering team has been notified.", request);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    /**
     * Helper to enforce consistent layout properties across all error handlers.
     */
    private Map<String, Object> createErrorBody(HttpStatus status, String message, HttpServletRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getRequestURI());
        return body;
    }
}
