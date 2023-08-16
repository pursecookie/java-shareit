package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingDtoForOwner;

import java.util.List;

@Data
@AllArgsConstructor
public class ItemDtoWithComments {
    private long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingDtoForOwner lastBooking;
    private BookingDtoForOwner nextBooking;
    private List<CommentDto> comments;
}