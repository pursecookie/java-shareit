package ru.practicum.shareit.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ItemAvailabilityException;
import ru.practicum.shareit.exception.NotFoundException;

@RestControllerAdvice
public class ErrorHandlingControllerAdvice {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Violation handleNotFoundException(NotFoundException e) {
        return new Violation(e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Violation handleAccessDeniedException(AccessDeniedException e) {
        return new Violation(e.getMessage());
    }

    @ExceptionHandler(ItemAvailabilityException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Violation handleItemAvailabilityException(ItemAvailabilityException e) {
        return new Violation(e.getMessage());
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Violation handleAnyException(Throwable e) {
        return new Violation(e.getMessage());
    }
}