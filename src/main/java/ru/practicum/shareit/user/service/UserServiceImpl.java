package ru.practicum.shareit.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserStorage userStorage, UserMapper userMapper) {
        this.userStorage = userStorage;
        this.userMapper = userMapper;
    }

    @Override
    public UserDto create(UserDto userDto) {
        User user = userMapper.mapToUser(userDto);

        return userMapper.mapToUserDto(userStorage.create(user));
    }

    @Override
    public UserDto read(long id) {
        if (userStorage.isNotExists(id)) {
            throw new NotFoundException("Данные с id " + id + " не найдены");
        }

        return userMapper.mapToUserDto(userStorage.read(id));
    }

    @Override
    public Collection<UserDto> readAll() {
        return userStorage.readAll().stream()
                .map(userMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto update(UserDto userDto, long id) {
        if (userStorage.isNotExists(id)) {
            throw new NotFoundException("Данные с id " + id + " не найдены");
        }

        User user = userMapper.mapToUser(userDto);

        user.setId(id);

        return userMapper.mapToUserDto(userStorage.update(user));
    }

    @Override
    public void delete(long id) {
        if (userStorage.isNotExists(id)) {
            throw new NotFoundException("Данные с id " + id + " не найдены");
        }

        userStorage.delete(id);
    }
}