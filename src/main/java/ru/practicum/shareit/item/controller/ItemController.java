package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") long ownerId,
                          @Valid @RequestBody ItemDto itemDto) {
        return itemService.create(ownerId, itemDto);
    }

    @GetMapping("/{id}")
    public ItemDto read(@RequestHeader("X-Sharer-User-Id") long userId,
                        @PathVariable long id) {
        return itemService.read(userId, id);
    }

    @GetMapping
    public Collection<ItemDto> readAll(@RequestHeader("X-Sharer-User-Id") long ownerId) {
        return itemService.readAll(ownerId);
    }

    @PatchMapping("/{id}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") long ownerId,
                          @RequestBody ItemDto itemDto, @PathVariable long id) {
        return itemService.update(ownerId, itemDto, id);
    }

    @GetMapping("/search")
    public Collection<ItemDto> search(@RequestParam String text) {
        return itemService.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@RequestHeader("X-Sharer-User-Id") long authorId,
                                    @Valid @RequestBody CommentDto commentDto, @PathVariable long itemId) {
        return itemService.createComment(authorId, commentDto, itemId);
    }

}