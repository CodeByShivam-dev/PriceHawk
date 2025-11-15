package com.pricehawk.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler
 * Returns consistent JSON error responses instead of HTML pages.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler
{

    @ExceptionHandler(InvalidQueryException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidQuery(InvalidQueryException ex) {
        log.warn("InvalidQueryException: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("error", "InvalidQuery");
        body.put("message", ex.getMessage());
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex) {
        log.error("Unhandled exception: ", ex);
        Map<String, Object> body = new HashMap<>();
        body.put("error", "InternalServerError");
        body.put("message", "Something went wrong. Please try again later.");
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
