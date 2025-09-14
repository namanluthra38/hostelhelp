package com.hostelhelp.hostelservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice

public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> handleValidationException(MethodArgumentNotValidException ex) {
        HashMap<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(HostelAlreadyExistsException.class)
    public ResponseEntity<Map<String,String>> handleHostelAlreadyExistsException(HostelAlreadyExistsException ex) {
        log.warn(ex.getMessage());
        Map<String,String> errors = new HashMap<>();
        errors.put("hostel", "Hostel already exists");
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(HostelNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleHostelNotFoundException(HostelNotFoundException ex) {
        log.warn(ex.getMessage());
        Map<String,String> errors = new HashMap<>();
        errors.put("hostel", "Hostel not found");
        return ResponseEntity.badRequest().body(errors);
    }
}
