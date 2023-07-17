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

    @Autowired
    public UserServiceImpl(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public UserDto create(UserDto userDto) {
        User user = UserMapper.mapToUser(userDto);

        return UserMapper.mapToUserDto(userStorage.create(user));
    }

    @Override
    public UserDto read(long id) {
        if (userStorage.isNotExists(id)) {
            throw new NotFoundException("Данные с id " + id + " не найдены");
        }

        return UserMapper.mapToUserDto(userStorage.read(id));
    }

    @Override
    public Collection<UserDto> readAll() {
        return userStorage.readAll().stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto update(UserDto userDto, long id) {
        if (userStorage.isNotExists(id)) {
            throw new NotFoundException("Данные с id " + id + " не найдены");
        }

        User user = UserMapper.mapToUser(userDto);

        user.setId(id);

        return UserMapper.mapToUserDto(userStorage.update(user));
    }

    @Override
    public void delete(long id) {
        if (userStorage.isNotExists(id)) {
            throw new NotFoundException("Данные с id " + id + " не найдены");
        }

        userStorage.delete(id);
    }
}