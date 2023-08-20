package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.ArrayList;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/items")
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                         @Valid @RequestBody ItemDtoInput itemDtoInput) {
        return itemClient.create(ownerId, itemDtoInput);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> read(@RequestHeader("X-Sharer-User-Id") long userId,
                                       @PathVariable long id) {
        return itemClient.read(userId, id);
    }

    @GetMapping
    public ResponseEntity<Object> readAll(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                          @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                          @RequestParam(defaultValue = "10") @Min(1) @Max(200) Integer size) {

        return itemClient.readAll(ownerId, from, size);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                         @RequestBody ItemDtoInput itemDtoInput, @PathVariable long id) {
        return itemClient.update(ownerId, itemDtoInput, id);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                         @RequestParam String text,
                                         @RequestParam(value = "from",
                                                 defaultValue = "0") @Min(0) Integer from,
                                         @RequestParam(value = "size", defaultValue = "10")
                                         @Min(1) @Max(200) Integer size) {
        if (text.isBlank()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }

        return itemClient.search(ownerId, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader("X-Sharer-User-Id") long authorId,
                                                @Valid @RequestBody CommentDto commentDto, @PathVariable long itemId) {
        return itemClient.createComment(authorId, commentDto, itemId);
    }

}