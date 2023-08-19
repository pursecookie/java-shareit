package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDtoWithRequestId create(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                       @RequestBody ItemDtoInput itemDtoInput) {
        return itemService.create(ownerId, itemDtoInput);
    }

    @GetMapping("/{id}")
    public ItemDtoWithComments read(@RequestHeader("X-Sharer-User-Id") long userId,
                                    @PathVariable long id) {
        return itemService.read(userId, id);
    }

    @GetMapping
    public Collection<ItemDtoWithComments> readAll(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                                   @RequestParam(defaultValue = "0") Integer from,
                                                   @RequestParam(defaultValue = "10") Integer size) {

        Pageable pageable = PageRequest.of(from / size, size);

        return itemService.readAll(ownerId, pageable);
    }

    @PatchMapping("/{id}")
    public ItemDtoWithRequestId update(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                       @RequestBody ItemDtoInput itemDtoInput, @PathVariable long id) {
        return itemService.update(ownerId, itemDtoInput, id);
    }

    @GetMapping("/search")
    public Collection<ItemDtoOutput> search(@RequestParam String text,
                                            @RequestParam(defaultValue = "0") Integer from,
                                            @RequestParam(defaultValue = "10") Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);

        return itemService.search(text, pageable);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@RequestHeader("X-Sharer-User-Id") long authorId,
                                    @RequestBody CommentDto commentDto, @PathVariable long itemId) {
        return itemService.createComment(authorId, commentDto, itemId);
    }

}