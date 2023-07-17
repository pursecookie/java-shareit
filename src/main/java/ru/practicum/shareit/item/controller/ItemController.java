package ru.practicum.shareit.item.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") long ownerId,
                          @Valid @RequestBody ItemDto itemDto) {
        return itemService.create(ownerId, itemDto);
    }

    @GetMapping("/{id}")
    public ItemDto read(@PathVariable long id) {
        return itemService.read(id);
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
    public Collection<ItemDto> search(@RequestParam("text") String text) {
        return itemService.search(text);
    }

}