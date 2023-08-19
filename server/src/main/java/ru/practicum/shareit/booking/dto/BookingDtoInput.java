package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingDtoInput {
    private long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private long itemId;
}