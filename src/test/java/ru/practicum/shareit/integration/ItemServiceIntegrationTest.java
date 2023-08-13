package ru.practicum.shareit.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ItemAvailabilityException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDtoInput;
import ru.practicum.shareit.request.dto.ItemRequestDtoOutput;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ItemServiceIntegrationTest {
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private ItemRequestService itemRequestService;
    UserDto userDto1, userDto2;
    ItemDtoInput itemDtoInput1, itemDtoInput2;
    ItemDtoInput updatedItemDtoInput, itemDtoInputWithoutName, itemDtoInputWithoutDescription,
            itemDtoInputWithoutAvailable, itemDtoInputNothingToUpdate;
    BookingDtoInput lastBooking, nextBooking;
    Pageable pageable;
    CommentDto commentDto;

    @BeforeEach
    void setUp() {
        userDto1 = new UserDto(1, "User1", "user1@mail.ru");
        userDto2 = new UserDto(2, "User2", "user2@mail.ru");

        itemDtoInput1 = new ItemDtoInput(1, "Item1", "Item1 Description", true, 0);
        itemDtoInput2 = new ItemDtoInput(2, "Item2", "Item2 Description", true, 0);
        updatedItemDtoInput = new ItemDtoInput(1, "Item1 Updated",
                "Item1 Description Updated", false, 0);

        lastBooking = new BookingDtoInput(1,
                LocalDateTime.of(2023, 8, 1, 10, 0, 0),
                LocalDateTime.of(2023, 8, 8, 10, 0, 0),
                1);
        nextBooking = new BookingDtoInput(2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2), 1);

        pageable = PageRequest.of(0, 10);

        commentDto = new CommentDto(1, "Good item", "User2",
                LocalDateTime.of(2023, 8, 8, 12, 0, 0));
    }

    @Test
    void create_whenAllIsOkWithoutRequestId_thenReturnedItem() {
        UserDto savedOwner = userService.create(userDto1);

        ItemDtoWithRequestId savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);

        assertEquals(itemDtoInput1.getName(), savedItem.getName());
        assertEquals(itemDtoInput1.getDescription(), savedItem.getDescription());
        assertEquals(itemDtoInput1.getAvailable(), savedItem.getAvailable());
        assertEquals(itemDtoInput1.getRequestId(), savedItem.getRequestId());
    }

    @Test
    void create_whenAllIsOkWithRequestId_thenReturnedItem() {
        UserDto savedOwner = userService.create(userDto1);
        UserDto savedRequestor = userService.create(userDto2);
        ItemRequestDtoInput itemRequestDtoInput = new ItemRequestDtoInput(1, "I need a description");
        ItemRequestDtoOutput itemRequestDtoOutput = itemRequestService.create(savedRequestor.getId(),
                itemRequestDtoInput);

        itemDtoInput1.setRequestId(itemRequestDtoOutput.getId());

        ItemDtoWithRequestId savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);

        assertEquals(itemDtoInput1.getName(), savedItem.getName());
        assertEquals(itemDtoInput1.getDescription(), savedItem.getDescription());
        assertEquals(itemDtoInput1.getAvailable(), savedItem.getAvailable());

        assertEquals(itemRequestDtoOutput.getId(), savedItem.getRequestId());
    }

    @Test
    void create_whenOwnerNotFound_thenNotFoundExceptionThrown() {
        Throwable thrown = assertThrows(NotFoundException.class, () -> itemService.create(9999, itemDtoInput1));
        assertEquals("Пользователь с id " + 9999 + " не найден", thrown.getMessage());
    }

    @Test
    void read_whenAllIsOkAndUserIsNotItemOwner_thenReturnedItemWithNullNextAndLastBookings() {
        UserDto savedOwner = userService.create(userDto1);
        UserDto savedUser = userService.create(userDto2);
        ItemDtoWithRequestId savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);

        ItemDtoWithComments returnedItem = itemService.read(savedUser.getId(), savedItem.getId());

        assertEquals(itemDtoInput1.getName(), returnedItem.getName());
        assertEquals(itemDtoInput1.getDescription(), returnedItem.getDescription());
        assertEquals(itemDtoInput1.getAvailable(), returnedItem.getAvailable());
        assertNull(returnedItem.getLastBooking());
        assertNull(returnedItem.getNextBooking());
    }

    @Test
    void read_whenUserNotFound_thenNotFoundExceptionThrown() {
        UserDto savedOwner = userService.create(userDto1);
        ItemDtoWithRequestId savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);

        Throwable thrown = assertThrows(NotFoundException.class, () -> itemService.read(9999, savedItem.getId()));
        assertEquals("Пользователь с id " + 9999 + " не найден", thrown.getMessage());
    }

    @Test
    void read_whenItemNotFound_thenNotFoundExceptionThrown() {
        UserDto savedOwner = userService.create(userDto1);

        Throwable thrown = assertThrows(NotFoundException.class, () -> itemService.read(savedOwner.getId(), 9999));
        assertEquals("Вещь с id " + 9999 + " не найдена", thrown.getMessage());
    }

    @Test
    void read_whenUserIsItemOwner_thenReturnedItemWithLastAndNextBookings() {
        UserDto savedOwner = userService.create(userDto1);
        UserDto savedBooker = userService.create(userDto2);
        ItemDtoWithRequestId savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);
        BookingDtoOutput savedLastBooking = bookingService.create(savedBooker.getId(), lastBooking);
        BookingDtoOutput savedNextBooking = bookingService.create(savedBooker.getId(), nextBooking);

        bookingService.updateApproval(savedOwner.getId(), savedLastBooking.getId(), true);
        bookingService.updateApproval(savedOwner.getId(), savedNextBooking.getId(), true);

        ItemDtoWithComments returnedItem = itemService.read(savedOwner.getId(), savedItem.getId());

        assertEquals(itemDtoInput1.getName(), returnedItem.getName());
        assertEquals(itemDtoInput1.getDescription(), returnedItem.getDescription());
        assertEquals(itemDtoInput1.getAvailable(), returnedItem.getAvailable());
        assertEquals(1, returnedItem.getLastBooking().getId());
        assertEquals(2, returnedItem.getLastBooking().getBookerId());
        assertEquals(2, returnedItem.getNextBooking().getId());
        assertEquals(2, returnedItem.getNextBooking().getBookerId());
    }

    @Test
    void readAll_whenAllIsOk_thenReturnedItemCollection() {
        UserDto savedOwner = userService.create(userDto1);
        UserDto savedBooker = userService.create(userDto2);
        ItemDtoWithRequestId savedItem1 = itemService.create(savedOwner.getId(), itemDtoInput1);
        ItemDtoWithRequestId savedItem2 = itemService.create(savedOwner.getId(), itemDtoInput2);
        BookingDtoOutput savedLastBooking = bookingService.create(savedBooker.getId(), lastBooking);
        BookingDtoOutput savedNextBooking = bookingService.create(savedBooker.getId(), nextBooking);

        bookingService.updateApproval(savedOwner.getId(), savedLastBooking.getId(), true);
        bookingService.updateApproval(savedOwner.getId(), savedNextBooking.getId(), true);

        List<ItemDtoWithRequestId> expectedItems = Stream.of(savedItem1, savedItem2)
                .sorted(Comparator.comparingLong(ItemDtoWithRequestId::getId))
                .collect(Collectors.toList());

        List<ItemDtoWithComments> returnedItems = new ArrayList<>(itemService.readAll(savedOwner.getId(), pageable));

        assertEquals(expectedItems.size(), returnedItems.size());

        assertEquals(expectedItems.get(0).getId(), returnedItems.get(0).getId());
        assertEquals(expectedItems.get(0).getName(), returnedItems.get(0).getName());
        assertEquals(expectedItems.get(0).getDescription(), returnedItems.get(0).getDescription());
        assertEquals(expectedItems.get(0).getAvailable(), returnedItems.get(0).getAvailable());
        assertEquals(1, returnedItems.get(0).getLastBooking().getId());
        assertEquals(2, returnedItems.get(0).getLastBooking().getBookerId());
        assertEquals(2, returnedItems.get(0).getNextBooking().getId());
        assertEquals(2, returnedItems.get(0).getNextBooking().getBookerId());

        assertEquals(expectedItems.get(1).getId(), returnedItems.get(1).getId());
        assertEquals(expectedItems.get(1).getName(), returnedItems.get(1).getName());
        assertEquals(expectedItems.get(1).getDescription(), returnedItems.get(1).getDescription());
        assertEquals(expectedItems.get(1).getAvailable(), returnedItems.get(1).getAvailable());
        assertNull(returnedItems.get(1).getLastBooking());
        assertNull(returnedItems.get(1).getNextBooking());
    }

    @Test
    void readAll_whenOwnerNotFound_thenNotFoundExceptionThrown() {
        Throwable thrown = assertThrows(NotFoundException.class, () -> itemService.readAll(9999, pageable));
        assertEquals("Пользователь с id " + 9999 + " не найден", thrown.getMessage());
    }

    @Test
    void update_whenAllIsOk_thenUpdatedItem() {
        UserDto savedOwner = userService.create(userDto1);
        ItemDtoWithRequestId savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);

        ItemDtoWithRequestId updatedItem = itemService
                .update(savedOwner.getId(), updatedItemDtoInput, savedItem.getId());

        assertEquals(updatedItemDtoInput.getName(), updatedItem.getName());
        assertEquals(updatedItemDtoInput.getDescription(), updatedItem.getDescription());
        assertEquals(updatedItemDtoInput.getAvailable(), updatedItem.getAvailable());
    }

    @Test
    void update_whenItemNotFound_thenNotFoundExceptionThrown() {
        UserDto savedOwner = userService.create(userDto1);

        Throwable thrown = assertThrows(NotFoundException.class,
                () -> itemService.update(savedOwner.getId(), updatedItemDtoInput, 9999));
        assertEquals("Вещь с id " + 9999 + " не найдена", thrown.getMessage());
    }

    @Test
    void update_whenUserIsNotOwner_thenAccessDeniedExceptionThrown() {
        UserDto savedOwner = userService.create(userDto1);
        UserDto savedUser = userService.create(userDto2);
        ItemDtoWithRequestId savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);

        Throwable thrown = assertThrows(AccessDeniedException.class,
                () -> itemService.update(savedUser.getId(), updatedItemDtoInput, savedItem.getId()));
        assertEquals("Нет прав для редактирования вещи", thrown.getMessage());
    }

    @Test
    void update_whenItemNameIsNull_thenUpdateOnlyDescriptionAndAvailable() {
        UserDto savedOwner = userService.create(userDto1);
        ItemDtoWithRequestId savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);
        itemDtoInputWithoutName = new ItemDtoInput();

        itemDtoInputWithoutName.setDescription("Item1 Description Updated");
        itemDtoInputWithoutName.setAvailable(false);

        ItemDtoWithRequestId updatedItem = itemService.update(savedOwner.getId(),
                itemDtoInputWithoutName, savedItem.getId());

        assertEquals(itemDtoInput1.getName(), updatedItem.getName());
        assertEquals(itemDtoInputWithoutName.getDescription(), updatedItem.getDescription());
        assertEquals(itemDtoInputWithoutName.getAvailable(), updatedItem.getAvailable());
    }

    @Test
    void update_whenItemDescriptionIsNull_thenUpdateOnlyNameAndAvailable() {
        UserDto savedOwner = userService.create(userDto1);
        ItemDtoWithRequestId savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);
        itemDtoInputWithoutDescription = new ItemDtoInput();

        itemDtoInputWithoutDescription.setName("Item1 Updated");
        itemDtoInputWithoutDescription.setAvailable(false);

        ItemDtoWithRequestId updatedItem = itemService.update(savedOwner.getId(),
                itemDtoInputWithoutDescription, savedItem.getId());

        assertEquals(itemDtoInputWithoutDescription.getName(), updatedItem.getName());
        assertEquals(itemDtoInput1.getDescription(), updatedItem.getDescription());
        assertEquals(itemDtoInputWithoutDescription.getAvailable(), updatedItem.getAvailable());
    }

    @Test
    void update_whenItemAvailableIsNull_thenUpdateOnlyNameAndDescription() {
        UserDto savedOwner = userService.create(userDto1);
        ItemDtoWithRequestId savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);
        itemDtoInputWithoutAvailable = new ItemDtoInput();

        itemDtoInputWithoutAvailable.setName("Item1 Updated");
        itemDtoInputWithoutAvailable.setDescription("Item1 Description Updated");

        ItemDtoWithRequestId updatedItem = itemService.update(savedOwner.getId(),
                itemDtoInputWithoutAvailable, savedItem.getId());

        assertEquals(itemDtoInputWithoutAvailable.getName(), updatedItem.getName());
        assertEquals(itemDtoInputWithoutAvailable.getDescription(), updatedItem.getDescription());
        assertEquals(itemDtoInput1.getAvailable(), updatedItem.getAvailable());
    }

    @Test
    void update_whenItemNameAndDescriptionAndAvailableAreNull_thenNothingToUpdate() {
        UserDto savedOwner = userService.create(userDto1);
        ItemDtoWithRequestId savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);
        itemDtoInputNothingToUpdate = new ItemDtoInput();

        ItemDtoWithRequestId updatedItem = itemService.update(savedOwner.getId(),
                itemDtoInputNothingToUpdate, savedItem.getId());

        assertEquals(itemDtoInput1.getName(), updatedItem.getName());
        assertEquals(itemDtoInput1.getDescription(), updatedItem.getDescription());
        assertEquals(itemDtoInput1.getAvailable(), updatedItem.getAvailable());
    }

    @Test
    void search_whenTextIsNotEmpty_thenReturnedSuitableItem() {
        UserDto savedOwner = userService.create(userDto1);
        ItemDtoWithRequestId savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);

        List<ItemDtoOutput> returnedItems = new ArrayList<>(itemService.search("description", pageable));

        assertEquals(savedItem.getName(), returnedItems.get(0).getName());
        assertEquals(savedItem.getDescription(), returnedItems.get(0).getDescription());
        assertEquals(savedItem.getAvailable(), returnedItems.get(0).getAvailable());
    }

    @Test
    void search_whenTextIsEmpty_thenReturnedEmptyCollection() {
        UserDto savedOwner = userService.create(userDto1);

        itemService.create(savedOwner.getId(), itemDtoInput1);

        Collection<ItemDtoOutput> returnedItems = itemService.search("", pageable);

        assertEquals(0, returnedItems.size());
    }

    @Test
    void createComment_whenAllIsOk_thenReturnedComment() {
        UserDto savedOwner = userService.create(userDto1);
        UserDto savedAuthor = userService.create(userDto2);
        ItemDtoWithRequestId savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);
        BookingDtoOutput savedLastBooking = bookingService.create(savedAuthor.getId(), lastBooking);

        bookingService.updateApproval(savedOwner.getId(), savedLastBooking.getId(), true);

        CommentDto savedComment = itemService.createComment(savedAuthor.getId(), commentDto, savedItem.getId());

        assertEquals(commentDto.getText(), savedComment.getText());
        assertEquals(commentDto.getAuthorName(), savedComment.getAuthorName());
        assertEquals(commentDto.getCreated(), savedComment.getCreated());
    }

    @Test
    void createComment_whenAuthorNotFound_thenNotFoundExceptionThrown() {
        UserDto savedOwner = userService.create(userDto1);
        ItemDtoWithRequestId savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);

        Throwable thrown = assertThrows(NotFoundException.class,
                () -> itemService.createComment(9999, commentDto, savedItem.getId()));
        assertEquals("Пользователь с id " + 9999 + " не найден", thrown.getMessage());
    }

    @Test
    void createComment_whenItemNotFound_thenNotFoundExceptionThrown() {
        UserDto savedAuthor = userService.create(userDto2);

        Throwable thrown = assertThrows(NotFoundException.class,
                () -> itemService.createComment(savedAuthor.getId(), commentDto, 9999));
        assertEquals("Вещь с id " + 9999 + " не найдена", thrown.getMessage());
    }

    @Test
    void createComment_whenAuthorDidNotBookItem_thenItemAvailabilityExceptionThrown() {
        UserDto savedOwner = userService.create(userDto1);
        UserDto savedAuthor = userService.create(userDto2);
        ItemDtoWithRequestId savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);

        Throwable thrown = assertThrows(ItemAvailabilityException.class,
                () -> itemService.createComment(savedAuthor.getId(), commentDto, savedItem.getId()));
        assertEquals("Пользователь с id " + savedAuthor.getId() +
                " не бронировал вещь с id " + savedItem.getId(), thrown.getMessage());
    }

    @Test
    void createComment_whenBookingNotOver_thenItemAvailabilityExceptionThrown() {
        UserDto savedOwner = userService.create(userDto1);
        UserDto savedAuthor = userService.create(userDto2);
        ItemDtoWithRequestId savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);
        BookingDtoOutput savedNextBooking = bookingService.create(savedAuthor.getId(), nextBooking);

        bookingService.updateApproval(savedOwner.getId(), savedNextBooking.getId(), true);

        Throwable thrown = assertThrows(ItemAvailabilityException.class,
                () -> itemService.createComment(savedAuthor.getId(), commentDto, savedItem.getId()));
        assertEquals("Отзыв можно оставить только после состоявшегося бронирования", thrown.getMessage());
    }
}