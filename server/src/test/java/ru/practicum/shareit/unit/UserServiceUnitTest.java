package ru.practicum.shareit.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;
    UserDto userDto;
    UserDto allUpdatedUserDto;
    User user;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(1L, "User", "user@mail.ru");
        user = UserMapper.mapToUser(userDto);
    }

    @Test
    void create_whenAllIsOk_ThenCreatedUser() {
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user);

        UserDto createdUser = userService.create(userDto);

        Mockito.verify(userRepository).save(Mockito.any());

        assertEquals(userDto.getId(), createdUser.getId());
        assertEquals(userDto.getName(), createdUser.getName());
        assertEquals(userDto.getEmail(), createdUser.getEmail());
    }

    @Test
    void read_whenUserFound_thenReturnedUser() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto returnedUser = userService.read(1);

        Mockito.verify(userRepository).findById(1L);

        assertEquals(userDto.getId(), returnedUser.getId());
        assertEquals(userDto.getName(), returnedUser.getName());
        assertEquals(userDto.getEmail(), returnedUser.getEmail());
    }

    @Test
    void read_whenUserNotFound_thenNotFoundExceptionThrown() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> userService.read(999));

        assertEquals("Пользователь с id " + 999 + " не найден", notFoundException.getMessage());
    }

    @Test
    void update_whenAllIsOk_thenUpdatedUser() {
        allUpdatedUserDto = new UserDto(1, "User Updated", "updated@mail.ru");

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(UserMapper.mapToUser(allUpdatedUserDto));

        UserDto updatedUser = userService.update(allUpdatedUserDto, 1);

        Mockito.verify(userRepository).save(Mockito.any());

        assertEquals(allUpdatedUserDto.getName(), updatedUser.getName());
        assertEquals(allUpdatedUserDto.getEmail(), updatedUser.getEmail());
    }

    @Test
    void update_whenUserNotFound_thenNotFoundExceptionThrown() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> userService.update(allUpdatedUserDto, 999));

        assertEquals("Пользователь с id " + 999 + " не найден", notFoundException.getMessage());
    }

    @Test
    void update_whenUserNameIsNull_thenUpdateOnlyEmail() {
        UserDto newEmailUserDto = new UserDto();

        newEmailUserDto.setEmail("updated@mail.ru");

        UserDto emailUpdatedUserDto = new UserDto(1, "User", "updated@mail.ru");

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(UserMapper.mapToUser(emailUpdatedUserDto));

        UserDto updatedUser = userService.update(newEmailUserDto, 1);

        Mockito.verify(userRepository).save(Mockito.any());

        assertEquals(userDto.getName(), updatedUser.getName());
        assertEquals(newEmailUserDto.getEmail(), updatedUser.getEmail());
    }

    @Test
    void update_whenUserEmailIsNull_thenUpdateOnlyName() {
        UserDto newNameUserDto = new UserDto();

        newNameUserDto.setName("User Updated");

        UserDto nameUpdatedUserDto = new UserDto(1, "User Updated", "user@mail.ru");

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(UserMapper.mapToUser(nameUpdatedUserDto));

        UserDto updatedUser = userService.update(newNameUserDto, 1);

        Mockito.verify(userRepository).save(Mockito.any());

        assertEquals(newNameUserDto.getName(), updatedUser.getName());
        assertEquals(userDto.getEmail(), updatedUser.getEmail());
    }

    @Test
    void update_whenUserNameAndEmailAreNull_thenNothingToUpdate() {
        UserDto emptyUserDto = new UserDto();

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(UserMapper.mapToUser(userDto));

        UserDto updatedUser = userService.update(emptyUserDto, 1);

        Mockito.verify(userRepository).save(Mockito.any());

        assertEquals(userDto.getName(), updatedUser.getName());
        assertEquals(userDto.getEmail(), updatedUser.getEmail());
    }

    @Test
    void delete_whenUserNotFound_thenNotFoundExceptionThrown() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> userService.delete(999));

        assertEquals("Пользователь с id " + 999 + " не найден", notFoundException.getMessage());
    }
}