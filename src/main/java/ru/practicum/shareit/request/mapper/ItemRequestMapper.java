package ru.practicum.shareit.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemDtoWithRequestId;
import ru.practicum.shareit.request.dto.ItemRequestDtoInput;
import ru.practicum.shareit.request.dto.ItemRequestDtoOutput;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class ItemRequestMapper {
    public ItemRequest mapToItemRequest(ItemRequestDtoInput itemRequestDtoInput,
                                        User requestor, LocalDateTime created) {
        return new ItemRequest(itemRequestDtoInput.getId(),
                itemRequestDtoInput.getDescription(),
                requestor,
                created);
    }

    public ItemRequestDtoOutput mapToItemRequestDtoOutput(ItemRequest itemRequest, List<ItemDtoWithRequestId> items) {
        if (items == null) {
            items = new ArrayList<>();
        }

        return new ItemRequestDtoOutput(itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreated(),
                items);
    }
}