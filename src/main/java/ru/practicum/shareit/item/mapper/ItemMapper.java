package ru.practicum.shareit.item.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingDtoForOwner;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@UtilityClass
public class ItemMapper {
    public Item mapToItem(ItemDtoInput itemDto, User owner, ItemRequest itemRequest) {
        return new Item(itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                owner,
                itemRequest);
    }

    public ItemDtoOutput mapToItemDtoOutput(Item item, BookingDtoForOwner lastBooking,
                                            BookingDtoForOwner nextBooking) {
        return new ItemDtoOutput(item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                lastBooking,
                nextBooking);
    }

    public ItemDtoWithComments mapToItemDtoWithComments(Item item, BookingDtoForOwner lastBooking,
                                                        BookingDtoForOwner nextBooking, List<CommentDto> comments) {
        return new ItemDtoWithComments(item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                lastBooking,
                nextBooking,
                comments);
    }

    public ItemDtoWithRequestId mapToItemDtoWithRequestId(Item item, long requestId) {
        return new ItemDtoWithRequestId(item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                requestId);
    }
}