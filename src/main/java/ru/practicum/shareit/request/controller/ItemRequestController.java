package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDtoInput;
import ru.practicum.shareit.request.dto.ItemRequestDtoOutput;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Collection;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDtoOutput create(@RequestHeader("X-Sharer-User-Id") long requestorId,
                                       @Valid @RequestBody ItemRequestDtoInput itemRequestDtoInput) {
        return itemRequestService.create(requestorId, itemRequestDtoInput);
    }

    @GetMapping
    public Collection<ItemRequestDtoOutput> readAllRequestorRequests(@RequestHeader("X-Sharer-User-Id") long requestorId) {
        return itemRequestService.readAllRequestorRequests(requestorId);
    }

    @GetMapping("/all")
    public Collection<ItemRequestDtoOutput> readAllOtherUsersRequests(@RequestHeader("X-Sharer-User-Id") long userId,
                                                                      @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                                                      @RequestParam(defaultValue = "10")
                                                                      @Min(1) @Max(200) Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);

        return itemRequestService.readAllOtherUsersRequests(userId, pageable);
    }

    @GetMapping("/{id}")
    public ItemRequestDtoOutput read(@RequestHeader("X-Sharer-User-Id") long userId,
                                     @PathVariable long id) {
        return itemRequestService.read(userId, id);
    }
}