package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class UserStorage {
    private final Map<Long, User> userStorage = new HashMap<>();
    private int counter = 1;

    public User create(User user) {
        validateEmail(user.getEmail());

        user.setId(counter++);
        userStorage.put(user.getId(), user);

        return user;
    }

    public User read(long id) {
        return userStorage.get(id);
    }

    public Collection<User> readAll() {
        return userStorage.values();
    }

    public User update(User user) {
        User updatedUser = userStorage.get(user.getId());

        if (!updatedUser.getEmail().equals(user.getEmail())) {
            validateEmail(user.getEmail());
        }

        if (user.getName() != null) {
            updatedUser.setName(user.getName());
        }

        if (user.getEmail() != null) {
            updatedUser.setEmail(user.getEmail());
        }

        return updatedUser;
    }

    public void delete(long id) {
        userStorage.remove(id);
    }

    public boolean isNotExists(long id) {
        return !userStorage.containsKey(id);
    }

    private void validateEmail(String email) {
        final Collection<String> emails = readAll().stream()
                .map(User::getEmail)
                .collect(Collectors.toList());

        if (emails.contains(email)) {
            throw new ValidationException("Пользователь с указанным email уже существует");
        }
    }
}