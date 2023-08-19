package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody UserDto userDto) {
        return userClient.create(userDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> read(@PathVariable long id) {
        return userClient.read(id);
    }

    @GetMapping
    public ResponseEntity<Object> readAll() {
        return userClient.readAll();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@RequestBody UserDto userDto, @PathVariable long id) {
        return userClient.update(userDto, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable long id) {
        return userClient.delete(id);
    }
}