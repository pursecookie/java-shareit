package ru.practicum.shareit.item.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.dto.*;

import java.util.Collection;

public interface ItemService {
    ItemDtoWithRequestId create(long ownerId, ItemDtoInput itemDtoInput);

    ItemDtoWithComments read(long userId, long id);

    Collection<ItemDtoWithComments> readAll(long ownerId, Pageable pageable);

    ItemDtoWithRequestId update(long ownerId, ItemDtoInput itemDto, long id);

    Collection<ItemDtoOutput> search(String text, Pageable pageable);

    CommentDto createComment(long authorId, CommentDto commentDto, long itemId);
}