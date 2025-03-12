package ru.practicum.exception;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Collections;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleDataValidationException(DataValidationException e) {
        StringWriter out = new StringWriter();
        e.printStackTrace(new PrintWriter(out));
        String stackTrace = out.toString();
        return new ApiError(HttpStatus.BAD_REQUEST, "Data validation exception", e.getMessage(),
                Collections.singletonList(stackTrace), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(NotFoundException e) {
        StringWriter out = new StringWriter();
        e.printStackTrace(new PrintWriter(out));
        String stackTrace = out.toString();
        return new ApiError(HttpStatus.NOT_FOUND, "Not found", e.getMessage(),
                Collections.singletonList(stackTrace), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequestException(BadRequestException e) {
        StringWriter out = new StringWriter();
        e.printStackTrace(new PrintWriter(out));
        String stackTrace = out.toString();
        return new ApiError(HttpStatus.BAD_REQUEST, "Bad request", e.getMessage(),
                Collections.singletonList(stackTrace), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataConflictException(DataConflictException e) {
        StringWriter out = new StringWriter();
        e.printStackTrace(new PrintWriter(out));
        String stackTrace = out.toString();
        return new ApiError(HttpStatus.CONFLICT, "Data conflict",
                e.getMessage(), Collections.singletonList(stackTrace), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConstraintViolationException(ConstraintViolationException e) {
        StringWriter out = new StringWriter();
        e.printStackTrace(new PrintWriter(out));
        String stackTrace = out.toString();
        return new ApiError(HttpStatus.CONFLICT, "Integrity constraint has been violated.", e.getMessage(),
                Collections.singletonList(stackTrace), LocalDateTime.now());
    }
}
