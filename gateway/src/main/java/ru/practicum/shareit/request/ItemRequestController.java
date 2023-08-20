package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") long requestorId,
                                         @Valid @RequestBody ItemRequestDtoInput itemRequestDtoInput) {
        return itemRequestClient.create(requestorId, itemRequestDtoInput);
    }

    @GetMapping
    public ResponseEntity<Object> readAllRequestorRequests(@RequestHeader("X-Sharer-User-Id") long requestorId) {
        return itemRequestClient.readAllRequestorRequests(requestorId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> readAllOtherUsersRequests(@RequestHeader("X-Sharer-User-Id") long userId,
                                                            @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                                            @RequestParam(defaultValue = "10")
                                                            @Min(1) @Max(200) Integer size) {

        return itemRequestClient.readAllOtherUsersRequests(userId, from, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> read(@RequestHeader("X-Sharer-User-Id") long userId,
                                       @PathVariable long id) {
        return itemRequestClient.read(userId, id);
    }
}