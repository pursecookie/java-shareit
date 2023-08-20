package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDtoInput;
import ru.practicum.shareit.request.dto.ItemRequestDtoOutput;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDtoOutput create(@RequestHeader("X-Sharer-User-Id") long requestorId,
                                       @RequestBody ItemRequestDtoInput itemRequestDtoInput) {
        return itemRequestService.create(requestorId, itemRequestDtoInput);
    }

    @GetMapping
    public Collection<ItemRequestDtoOutput> readAllRequestorRequests(@RequestHeader("X-Sharer-User-Id") long requestorId) {
        return itemRequestService.readAllRequestorRequests(requestorId);
    }

    @GetMapping("/all")
    public Collection<ItemRequestDtoOutput> readAllOtherUsersRequests(@RequestHeader("X-Sharer-User-Id") long userId,
                                                                      @RequestParam(defaultValue = "0") Integer from,
                                                                      @RequestParam(defaultValue = "10") Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);

        return itemRequestService.readAllOtherUsersRequests(userId, pageable);
    }

    @GetMapping("/{id}")
    public ItemRequestDtoOutput read(@RequestHeader("X-Sharer-User-Id") long userId,
                                     @PathVariable long id) {
        return itemRequestService.read(userId, id);
    }
}