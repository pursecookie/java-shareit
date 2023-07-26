package ru.practicum.shareit.exception;

public class ItemAvailabilityException extends RuntimeException {
    public ItemAvailabilityException(String message) {
        super(message);
    }
}