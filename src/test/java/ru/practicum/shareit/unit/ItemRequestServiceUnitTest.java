package ru.practicum.shareit.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDtoInput;
import ru.practicum.shareit.request.dto.ItemRequestDtoOutput;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceUnitTest {
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;
    UserDto userDto1, userDto2;
    User user1, user2;
    ItemRequestDtoInput itemRequestDtoInput1, itemRequestDtoInput2;
    ItemRequest itemRequest1, itemRequest2;
    List<ItemRequest> requests;
    Pageable pageable;

    @BeforeEach
    void setUp() {
        userDto1 = new UserDto(1, "User1", "user1@mail.ru");
        user1 = UserMapper.mapToUser(userDto1);

        userDto2 = new UserDto(2, "User2", "user2@mail.ru");
        user2 = UserMapper.mapToUser(userDto2);

        itemRequestDtoInput1 = new ItemRequestDtoInput(1, "I need an item1");
        itemRequest1 = ItemRequestMapper.mapToItemRequest(itemRequestDtoInput1, user2, LocalDateTime.now());

        itemRequestDtoInput2 = new ItemRequestDtoInput(2, "something with description");
        itemRequest2 = ItemRequestMapper.mapToItemRequest(itemRequestDtoInput2, user2, LocalDateTime.now());

        requests = Stream.of(itemRequest1, itemRequest2)
                .sorted(Comparator.comparing(ItemRequest::getCreated).reversed())
                .collect(Collectors.toList());

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void create_whenAllIsOk_thenCreatedRequest() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(itemRequestRepository.save(Mockito.any())).thenReturn(itemRequest1);

        ItemRequestDtoOutput createdRequest = itemRequestService.create(2, itemRequestDtoInput1);

        Mockito.verify(itemRequestRepository).save(Mockito.any());

        assertEquals(itemRequestDtoInput1.getDescription(), createdRequest.getDescription());
        assertEquals(0, createdRequest.getItems().size());

        LocalDateTime expectedCreated = LocalDateTime.now();
        LocalDateTime actualCreated = createdRequest.getCreated();

        assertTrue(ChronoUnit.MILLIS.between(expectedCreated, actualCreated) < 1000);
    }

    @Test
    void create_whenRequestorNotFound_thenNotFoundExceptionThrown() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> itemRequestService.create(999, itemRequestDtoInput1));

        assertEquals("Пользователь с id " + 999 + " не найден", notFoundException.getMessage());
    }

    @Test
    void readAllRequestorRequests_whenAllIsOk_thenReturnedRequests() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(2))
                .thenReturn(requests);
        Mockito.when(itemRepository.findAllByItemRequestId(Mockito.anyLong()))
                .thenReturn(new ArrayList<>());

        List<ItemRequestDtoOutput> requestorRequests = new ArrayList<>(itemRequestService
                .readAllRequestorRequests(2));

        Mockito.verify(itemRequestRepository).findAllByRequestorIdOrderByCreatedDesc(2);

        assertEquals(2, requestorRequests.size());

        assertEquals(itemRequest1.getDescription(), requestorRequests.get(0).getDescription());

        LocalDateTime expectedCreated1 = itemRequest1.getCreated();
        LocalDateTime actualCreated1 = requestorRequests.get(0).getCreated();

        assertTrue(ChronoUnit.MILLIS.between(expectedCreated1, actualCreated1) < 1000);

        assertEquals(itemRequest2.getDescription(), requestorRequests.get(1).getDescription());

        LocalDateTime expectedCreated2 = itemRequest2.getCreated();
        LocalDateTime actualCreated2 = requestorRequests.get(1).getCreated();

        assertTrue(ChronoUnit.MILLIS.between(expectedCreated2, actualCreated2) < 1000);
    }

    @Test
    void readAllRequestorRequests_whenRequestorNotFound_thenNotFoundExceptionThrown() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> itemRequestService.readAllRequestorRequests(999));

        assertEquals("Пользователь с id " + 999 + " не найден", notFoundException.getMessage());
    }

    @Test
    void readAllOtherUsersRequests_whenAllIsOk_thenReturnedRequests() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        Mockito.when(itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(1, pageable))
                .thenReturn(new PageImpl<>(requests));
        Mockito.when(itemRepository.findAllByItemRequestId(Mockito.anyLong()))
                .thenReturn(new ArrayList<>());

        List<ItemRequestDtoOutput> otherUsersRequests = new ArrayList<>(itemRequestService
                .readAllOtherUsersRequests(1, pageable));

        Mockito.verify(itemRequestRepository).findAllByRequestorIdNotOrderByCreatedDesc(1, pageable);

        assertEquals(2, otherUsersRequests.size());

        assertEquals(itemRequest1.getDescription(), otherUsersRequests.get(0).getDescription());

        LocalDateTime expectedCreated1 = itemRequest1.getCreated();
        LocalDateTime actualCreated1 = otherUsersRequests.get(0).getCreated();

        assertTrue(ChronoUnit.MILLIS.between(expectedCreated1, actualCreated1) < 1000);

        assertEquals(itemRequest2.getDescription(), otherUsersRequests.get(1).getDescription());

        LocalDateTime expectedCreated2 = itemRequest2.getCreated();
        LocalDateTime actualCreated2 = otherUsersRequests.get(1).getCreated();

        assertTrue(ChronoUnit.MILLIS.between(expectedCreated2, actualCreated2) < 1000);
    }

    @Test
    void readAllOtherUsersRequests_whenUserNotFound_thenNotFoundExceptionThrown() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> itemRequestService.readAllOtherUsersRequests(999, pageable));

        assertEquals("Пользователь с id " + 999 + " не найден", notFoundException.getMessage());
    }

    @Test
    void read_whenAllIsOk_thenReturnedRequest() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(itemRequest1));
        Mockito.when(itemRequestRepository.findByIdOrderByCreatedDesc(1L)).thenReturn(itemRequest1);
        Mockito.when(itemRepository.findAllByItemRequestId(Mockito.anyLong()))
                .thenReturn(new ArrayList<>());

        ItemRequestDtoOutput returnedRequest = itemRequestService.read(2, 1);

        Mockito.verify(itemRequestRepository).findByIdOrderByCreatedDesc(1L);

        assertEquals(itemRequestDtoInput1.getDescription(), returnedRequest.getDescription());
        assertEquals(0, returnedRequest.getItems().size());

        LocalDateTime expectedCreated = LocalDateTime.now();
        LocalDateTime actualCreated = returnedRequest.getCreated();

        assertTrue(ChronoUnit.MILLIS.between(expectedCreated, actualCreated) < 1000);
    }

    @Test
    void read_whenUserNotFound_thenNotFoundExceptionThrown() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> itemRequestService.read(999, 1));

        assertEquals("Пользователь с id " + 999 + " не найден", notFoundException.getMessage());
    }

    @Test
    void read_whenItemNotFound_thenNotFoundExceptionThrown() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(itemRequestRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> itemRequestService.read(2, 999));

        assertEquals("Запрос на вещь с id " + 999 + " не найден", notFoundException.getMessage());
    }
}