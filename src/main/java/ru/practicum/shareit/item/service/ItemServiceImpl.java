package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final ItemMapper itemMapper;

    @Autowired
    public ItemServiceImpl(ItemStorage itemStorage, UserStorage userStorage,
                           ItemMapper itemMapper) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
        this.itemMapper = itemMapper;
    }

    @Override
    public ItemDto create(long ownerId, ItemDto itemDto) {
        if (userStorage.isNotExists(ownerId)) {
            throw new NotFoundException("Пользователь с id " + ownerId + " не найден");
        }

        Item item = itemMapper.mapToItem(itemDto);

        item.setOwnerId(ownerId);

        return itemMapper.mapToItemDto(itemStorage.create(item));
    }

    @Override
    public ItemDto read(long id) {
        if (itemStorage.isNotExists(id)) {
            throw new NotFoundException("Вещь с id " + id + " не найдена");
        }

        return itemMapper.mapToItemDto(itemStorage.read(id));
    }

    @Override
    public Collection<ItemDto> readAll(long ownerId) {
        return itemStorage.readAll(ownerId).stream()
                .map(itemMapper::mapToItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto update(long ownerId, ItemDto itemDto, long id) {
        if (itemStorage.isNotExists(id)) {
            throw new NotFoundException("Вещь с id " + id + " не найдена");
        }

        if (itemStorage.read(id).getOwnerId() != ownerId) {
            throw new AccessDeniedException("Нет прав для редактирования вещи");
        }

        Item item = itemMapper.mapToItem(itemDto);

        item.setId(id);
        item.setOwnerId(ownerId);

        return itemMapper.mapToItemDto(itemStorage.update(item));
    }

    @Override
    public Collection<ItemDto> search(String text) {
        return itemStorage.search(text).stream()
                .map(itemMapper::mapToItemDto)
                .collect(Collectors.toList());
    }
}