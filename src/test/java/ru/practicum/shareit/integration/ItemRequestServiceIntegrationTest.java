package ru.practicum.shareit.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDtoInput;
import ru.practicum.shareit.request.dto.ItemRequestDtoOutput;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ItemRequestServiceIntegrationTest {
    @Autowired
    private ItemRequestService itemRequestService;
    @Autowired
    private UserService userService;
    UserDto userDto1;
    UserDto userDto2;
    ItemRequestDtoInput itemRequestDtoInput1;
    ItemRequestDtoInput itemRequestDtoInput2;
    Pageable pageable;

    @BeforeEach
    void setUp() {
        userDto1 = new UserDto(1, "User1", "user1@mail.ru");
        userDto2 = new UserDto(2, "User2", "user2@mail.ru");

        itemRequestDtoInput1 = new ItemRequestDtoInput(1, "I need an item1");
        itemRequestDtoInput2 = new ItemRequestDtoInput(2, "something with description");

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void create_whenAllIsOk_thenCreatedRequest() {
        UserDto savedRequestor = userService.create(userDto2);

        ItemRequestDtoOutput savedRequest = itemRequestService.create(savedRequestor.getId(), itemRequestDtoInput1);

        assertEquals(itemRequestDtoInput1.getDescription(), savedRequest.getDescription());
        assertEquals(0, savedRequest.getItems().size());

        LocalDateTime expectedCreated = LocalDateTime.now();
        LocalDateTime actualCreated = savedRequest.getCreated();

        assertTrue(ChronoUnit.MILLIS.between(expectedCreated, actualCreated) < 1000);
    }

    @Test
    void create_whenRequestorNotFound_thenNotFoundExceptionThrown() {
        Throwable thrown = assertThrows(NotFoundException.class,
                () -> itemRequestService.create(9999, itemRequestDtoInput1));
        assertEquals("Пользователь с id " + 9999 + " не найден", thrown.getMessage());
    }

    @Test
    void readAllRequestorRequests_whenAllIsOk_thenReturnedRequests() {
        UserDto savedRequestor = userService.create(userDto2);

        ItemRequestDtoOutput savedRequest1 = itemRequestService.create(savedRequestor.getId(), itemRequestDtoInput1);
        ItemRequestDtoOutput savedRequest2 = itemRequestService.create(savedRequestor.getId(), itemRequestDtoInput2);

        List<ItemRequestDtoOutput> requestorRequests = new ArrayList<>(itemRequestService
                .readAllRequestorRequests(savedRequestor.getId()));

        assertEquals(2, requestorRequests.size());

        assertEquals(savedRequest2.getDescription(), requestorRequests.get(0).getDescription());
        assertEquals(savedRequest2.getItems(), requestorRequests.get(0).getItems());

        LocalDateTime expectedCreated1 = savedRequest2.getCreated();
        LocalDateTime actualCreated1 = requestorRequests.get(0).getCreated();

        assertTrue(ChronoUnit.MILLIS.between(expectedCreated1, actualCreated1) < 1000);

        assertEquals(savedRequest1.getDescription(), requestorRequests.get(1).getDescription());
        assertEquals(savedRequest1.getItems(), requestorRequests.get(1).getItems());

        LocalDateTime expectedCreated2 = savedRequest1.getCreated();
        LocalDateTime actualCreated2 = requestorRequests.get(1).getCreated();

        assertTrue(ChronoUnit.MILLIS.between(expectedCreated2, actualCreated2) < 1000);
    }

    @Test
    void readAllRequestorRequests_whenRequestorNotFound_thenNotFoundExceptionThrown() {
        Throwable thrown = assertThrows(NotFoundException.class,
                () -> itemRequestService.readAllRequestorRequests(9999));
        assertEquals("Пользователь с id " + 9999 + " не найден", thrown.getMessage());
    }

    @Test
    void readAllOtherUsersRequests_whenUserNotFound_thenNotFoundExceptionThrown() {
        Throwable thrown = assertThrows(NotFoundException.class,
                () -> itemRequestService.readAllOtherUsersRequests(9999, pageable));
        assertEquals("Пользователь с id " + 9999 + " не найден", thrown.getMessage());
    }

    @Test
    void read_whenAllIsOk_thenReturnedRequest() {
        UserDto savedRequestor = userService.create(userDto2);
        ItemRequestDtoOutput savedRequest = itemRequestService.create(savedRequestor.getId(), itemRequestDtoInput1);
        UserDto savedUser = userService.create(userDto1);

        ItemRequestDtoOutput returnedRequest = itemRequestService.read(savedUser.getId(), savedRequest.getId());

        assertEquals(itemRequestDtoInput1.getDescription(), returnedRequest.getDescription());
        assertEquals(0, returnedRequest.getItems().size());

        LocalDateTime expectedCreated = LocalDateTime.now();
        LocalDateTime actualCreated = returnedRequest.getCreated();

        assertTrue(ChronoUnit.MILLIS.between(expectedCreated, actualCreated) < 1000);
    }

    @Test
    void read_whenUserNotFound_thenNotFoundExceptionThrown() {
        Throwable thrown = assertThrows(NotFoundException.class, () -> itemRequestService.read(9999, 1));
        assertEquals("Пользователь с id " + 9999 + " не найден", thrown.getMessage());
    }

    @Test
    void read_whenItemNotFound_thenNotFoundExceptionThrown() {
        UserDto savedRequestor = userService.create(userDto2);

        Throwable thrown = assertThrows(NotFoundException.class,
                () -> itemRequestService.read(savedRequestor.getId(), 999));
        assertEquals("Запрос на вещь с id " + 999 + " не найден", thrown.getMessage());
    }

}