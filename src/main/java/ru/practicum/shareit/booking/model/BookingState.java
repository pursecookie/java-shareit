package ru.practicum.shareit.booking.model;

import java.util.Optional;

public enum BookingState {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static Optional<BookingState> from(String strState) {
        for (BookingState state : values()) {
            if (state.name().equalsIgnoreCase(strState)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }
}