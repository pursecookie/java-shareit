package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {
    UserDto create(UserDto userDto);

    UserDto read(long id);

    Collection<UserDto> readAll();

    UserDto update(UserDto userDto, long id);

    void delete(long id);
}