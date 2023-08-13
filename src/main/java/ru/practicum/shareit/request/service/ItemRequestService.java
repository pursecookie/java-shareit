package ru.practicum.shareit.request.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.dto.ItemRequestDtoInput;
import ru.practicum.shareit.request.dto.ItemRequestDtoOutput;

import java.util.Collection;

public interface ItemRequestService {
    ItemRequestDtoOutput create(long requestorId, ItemRequestDtoInput itemRequestDtoInput);

    Collection<ItemRequestDtoOutput> readAllRequestorRequests(long requestorId);

    Collection<ItemRequestDtoOutput> readAllOtherUsersRequests(long userId, Pageable pageable);

    ItemRequestDtoOutput read(long userId, long id);
}