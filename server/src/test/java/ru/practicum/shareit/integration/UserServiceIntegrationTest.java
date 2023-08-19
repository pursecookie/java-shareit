package ru.practicum.shareit.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserServiceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class UserServiceIntegrationTest {
    @Autowired
    private UserServiceImpl userService;
    UserDto userDto;
    UserDto allUpdatedUserDto;
    UserDto emailUpdatedUserDto;
    UserDto nameUpdatedUserDto;
    UserDto nothingToUpdateUserDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(1L, "User", "user@mail.ru");
    }

    @Test
    void create_whenAllIsOk_ThenCreatedUser() {
        UserDto savedUser = userService.create(userDto);

        assertEquals(userDto.getId(), savedUser.getId());
        assertEquals(userDto.getName(), savedUser.getName());
        assertEquals(userDto.getEmail(), savedUser.getEmail());
    }

    @Test
    void read_whenUserFound_thenReturnedUser() {
        UserDto savedUser = userService.create(userDto);

        UserDto returnedUser = userService.read(savedUser.getId());

        assertEquals(userDto.getId(), returnedUser.getId());
        assertEquals(userDto.getName(), returnedUser.getName());
        assertEquals(userDto.getEmail(), returnedUser.getEmail());
    }

    @Test
    void read_whenUserNotFound_thenNotFoundExceptionThrown() {
        Throwable thrown = assertThrows(NotFoundException.class, () -> userService.read(9999));
        assertEquals("Пользователь с id " + 9999 + " не найден", thrown.getMessage());
    }

    @Test
    void update_whenAllIsOk_thenUpdatedUser() {
        allUpdatedUserDto = new UserDto(1, "User Updated", "updated@mail.ru");
        UserDto savedUser = userService.create(userDto);

        UserDto updatedUser = userService.update(allUpdatedUserDto, savedUser.getId());

        assertEquals(allUpdatedUserDto.getId(), updatedUser.getId());
        assertEquals(allUpdatedUserDto.getName(), updatedUser.getName());
        assertEquals(allUpdatedUserDto.getEmail(), updatedUser.getEmail());
    }

    @Test
    void update_whenUserNotFound_thenNotFoundExceptionThrown() {
        allUpdatedUserDto = new UserDto(1, "User Updated", "updated@mail.ru");

        Throwable thrown = assertThrows(NotFoundException.class, () -> userService.update(allUpdatedUserDto, 9999));
        assertEquals("Пользователь с id " + 9999 + " не найден", thrown.getMessage());
    }

    @Test
    void update_whenUserNameIsNull_thenUpdateOnlyEmail() {
        emailUpdatedUserDto = new UserDto();

        emailUpdatedUserDto.setId(1);
        emailUpdatedUserDto.setEmail("updated@mail.ru");

        UserDto savedUser = userService.create(userDto);

        UserDto updatedUser = userService.update(emailUpdatedUserDto, savedUser.getId());

        assertEquals(emailUpdatedUserDto.getId(), updatedUser.getId());
        assertEquals(userDto.getName(), updatedUser.getName());
        assertEquals(emailUpdatedUserDto.getEmail(), updatedUser.getEmail());
    }

    @Test
    void update_whenUserEmailIsNull_thenUpdateOnlyName() {
        nameUpdatedUserDto = new UserDto();

        nameUpdatedUserDto.setId(1);
        nameUpdatedUserDto.setName("User Updated");

        UserDto savedUser = userService.create(userDto);

        UserDto updatedUser = userService.update(nameUpdatedUserDto, savedUser.getId());

        assertEquals(nameUpdatedUserDto.getId(), updatedUser.getId());
        assertEquals(nameUpdatedUserDto.getName(), updatedUser.getName());
        assertEquals(userDto.getEmail(), updatedUser.getEmail());
    }

    @Test
    void update_whenUserNameAndEmailAreNull_thenNothingToUpdate() {
        nothingToUpdateUserDto = new UserDto();
        UserDto savedUser = userService.create(userDto);

        UserDto updatedUser = userService.update(nothingToUpdateUserDto, savedUser.getId());

        assertEquals(userDto.getId(), updatedUser.getId());
        assertEquals(userDto.getName(), updatedUser.getName());
        assertEquals(userDto.getEmail(), updatedUser.getEmail());
    }

    @Test
    void delete_whenUserNotFound_thenNotFoundExceptionThrown() {
        Throwable thrown = assertThrows(NotFoundException.class, () -> userService.delete(9999));
        assertEquals("Пользователь с id " + 9999 + " не найден", thrown.getMessage());
    }
}