package com.example.booking.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {
    record ErrorResponse(Instant timestamp, int status, String error, String message) { }
    @ExceptionHandler(EntityNotFoundException.class) ResponseEntity<ErrorResponse> notFound(EntityNotFoundException ex) { return error(HttpStatus.NOT_FOUND, ex.getMessage()); }
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class, ConstraintViolationException.class}) ResponseEntity<ErrorResponse> badRequest(Exception ex) { String message = ex instanceof MethodArgumentNotValidException validation ? validation.getBindingResult().getFieldErrors().stream().map(e -> e.getField() + ": " + e.getDefaultMessage()).collect(Collectors.joining(", ")) : ex.getMessage(); return error(HttpStatus.BAD_REQUEST, message); }
    @ExceptionHandler(BadCredentialsException.class) ResponseEntity<ErrorResponse> unauthorized(BadCredentialsException ex) { return error(HttpStatus.UNAUTHORIZED, "Invalid username or password"); }
    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message) { return ResponseEntity.status(status).body(new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message)); }
}
