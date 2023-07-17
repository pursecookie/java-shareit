package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {
    ItemDto create(long ownerId, ItemDto itemDto);

    ItemDto read(long id);

    Collection<ItemDto> readAll(long ownerId);

    ItemDto update(long ownerId, ItemDto itemDto, long id);

    Collection<ItemDto> search(String text);
}