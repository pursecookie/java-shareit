package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingDtoForOwner;

@Data
@AllArgsConstructor
public class ItemDtoOutput {
    private long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingDtoForOwner lastBooking;
    private BookingDtoForOwner nextBooking;
}