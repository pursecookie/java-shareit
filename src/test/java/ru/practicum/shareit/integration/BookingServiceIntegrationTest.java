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
import ru.practicum.shareit.booking.model.BookingApproval;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ItemAvailabilityException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDtoInput;
import ru.practicum.shareit.item.dto.ItemDtoWithRequestId;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class BookingServiceIntegrationTest {
    @Autowired
    private BookingService bookingService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    UserDto userDto1;
    UserDto userDto2;
    ItemDtoInput itemDtoInput1;
    ItemDtoInput itemDtoInput2;
    BookingDtoInput currentBooking;
    BookingDtoInput pastBooking;
    BookingDtoInput futureBooking;
    Pageable pageable;

    @BeforeEach
    void setUp() {
        userDto1 = new UserDto(1, "User1", "user1@mail.ru");
        userDto2 = new UserDto(2, "User2", "user2@mail.ru");

        itemDtoInput1 = new ItemDtoInput(1, "Item1", "Item1 Description", true, 0);
        itemDtoInput2 = new ItemDtoInput(2, "Item2", "Item2 Description", true, 0);

        currentBooking = new BookingDtoInput(1, LocalDateTime.now(), LocalDateTime.now().plusHours(2), 1);
        pastBooking = new BookingDtoInput(2,
                LocalDateTime.of(2023, 8, 1, 10, 0, 0),
                LocalDateTime.of(2023, 8, 8, 10, 0, 0),
                1);
        futureBooking = new BookingDtoInput(3, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2), 2);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void create_whenAllIsOk_thenReturnedBooking() {
        UserDto savedOwner = userService.create(userDto1);

        itemService.create(savedOwner.getId(), itemDtoInput1);

        UserDto savedBooker = userService.create(userDto2);

        BookingDtoOutput savedBooking = bookingService.create(savedBooker.getId(), currentBooking);

        assertEquals(currentBooking.getStart(), savedBooking.getStart());
        assertEquals(currentBooking.getEnd(), savedBooking.getEnd());
        assertEquals(currentBooking.getItemId(), savedBooking.getItem().getId());
    }

    @Test
    void create_whenBookerNotFound_thenNotFoundExceptionThrown() {
        Throwable thrown = assertThrows(NotFoundException.class,
                () -> bookingService.create(9999, currentBooking));
        assertEquals("Пользователь с id " + 9999 + " не найден", thrown.getMessage());
    }

    @Test
    void create_whenItemNotFound_thenNotFoundExceptionThrown() {
        UserDto savedBooker = userService.create(userDto2);

        BookingDtoInput wrongItemBooking = new BookingDtoInput(1, LocalDateTime.now(),
                LocalDateTime.now(), 999);

        Throwable thrown = assertThrows(NotFoundException.class,
                () -> bookingService.create(savedBooker.getId(), wrongItemBooking));
        assertEquals("Вещь с id " + wrongItemBooking.getItemId() + " не найдена", thrown.getMessage());
    }

    @Test
    void create_whenBookerIsOwner_thenNotFoundExceptionThrown() {
        UserDto savedOwner = userService.create(userDto1);

        itemService.create(savedOwner.getId(), itemDtoInput1);

        Throwable thrown = assertThrows(NotFoundException.class,
                () -> bookingService.create(savedOwner.getId(), currentBooking));
        assertEquals("Владелец вещи не может забронировать свою же вещь", thrown.getMessage());
    }

    @Test
    void create_whenItemUnavailable_thenItemAvailabilityExceptionThrown() {
        UserDto savedOwner = userService.create(userDto1);

        itemDtoInput1.setAvailable(false);
        itemService.create(savedOwner.getId(), itemDtoInput1);

        UserDto savedBooker = userService.create(userDto2);

        Throwable thrown = assertThrows(ItemAvailabilityException.class,
                () -> bookingService.create(savedBooker.getId(), currentBooking));
        assertEquals("Вещь с id " + currentBooking.getItemId() + " недоступна к бронированию",
                thrown.getMessage());
    }

    @Test
    void read_whenAllIsOk_thenReturnedBooking() {
        UserDto savedOwner = userService.create(userDto1);
        ItemDtoWithRequestId savedItem = itemService.create(savedOwner.getId(), itemDtoInput1);
        UserDto savedBooker = userService.create(userDto2);

        bookingService.create(savedBooker.getId(), pastBooking);

        BookingDtoOutput returnedBooking = bookingService.read(savedOwner.getId(), savedItem.getId());

        LocalDateTime expectedStart = pastBooking.getStart();
        LocalDateTime actualStart = returnedBooking.getStart();

        assertTrue(ChronoUnit.MILLIS.between(expectedStart, actualStart) < 1000);

        LocalDateTime expectedEnd = pastBooking.getEnd();
        LocalDateTime actualEnd = returnedBooking.getEnd();

        assertTrue(ChronoUnit.MILLIS.between(expectedEnd, actualEnd) < 1000);
        assertEquals(pastBooking.getItemId(), returnedBooking.getItem().getId());
    }

    @Test
    void read_whenUserNotFound_thenNotFoundExceptionThrown() {
        Throwable thrown = assertThrows(NotFoundException.class,
                () -> bookingService.read(9999, currentBooking.getId()));
        assertEquals("Пользователь с id " + 9999 + " не найден", thrown.getMessage());
    }

    @Test
    void read_whenBookingNotFound_thenNotFoundExceptionThrown() {
        UserDto savedOwner = userService.create(userDto1);

        Throwable thrown = assertThrows(NotFoundException.class,
                () -> bookingService.read(savedOwner.getId(), 999));
        assertEquals("Бронирование с id " + 999 + " не найдено", thrown.getMessage());
    }

    @Test
    void read_whenUserNotOwnerOrBooker_thenNotFoundExceptionThrown() {
        UserDto savedOwner = userService.create(userDto1);

        itemService.create(savedOwner.getId(), itemDtoInput1);

        UserDto savedBooker = userService.create(userDto2);
        BookingDtoOutput savedBooking = bookingService.create(savedBooker.getId(), currentBooking);
        UserDto otherUser = userService.create(new UserDto(3, "User3", "user3@mail.ru"));

        Throwable thrown = assertThrows(NotFoundException.class,
                () -> bookingService.read(otherUser.getId(), savedBooking.getId()));
        assertEquals("Бронирование с id " + savedBooking.getId() +
                " не найдено для пользователя с id " + otherUser.getId(), thrown.getMessage());
    }

    @Test
    void readAllBookerBookings_whenAllState_thenReturnAllBookings() {
        UserDto savedOwner = userService.create(userDto1);

        itemService.create(savedOwner.getId(), itemDtoInput1);
        itemService.create(savedOwner.getId(), itemDtoInput2);

        UserDto savedBooker = userService.create(userDto2);

        bookingService.create(savedBooker.getId(), currentBooking);
        bookingService.create(savedBooker.getId(), pastBooking);
        bookingService.create(savedBooker.getId(), futureBooking);

        List<BookingDtoOutput> allBookings = new ArrayList<>(bookingService
                .readAllBookerBookings(savedBooker.getId(), "ALL", pageable));

        assertEquals(3, allBookings.size());
    }

    @Test
    void readAllBookerBookings_whenCurrentState_thenReturnCurrentBookings() {
        UserDto savedOwner = userService.create(userDto1);

        itemService.create(savedOwner.getId(), itemDtoInput1);
        itemService.create(savedOwner.getId(), itemDtoInput2);

        UserDto savedBooker = userService.create(userDto2);

        bookingService.create(savedBooker.getId(), currentBooking);
        bookingService.create(savedBooker.getId(), pastBooking);
        bookingService.create(savedBooker.getId(), futureBooking);

        List<BookingDtoOutput> currentBookings = new ArrayList<>(bookingService
                .readAllBookerBookings(savedBooker.getId(), "CURRENT", pageable));

        assertEquals(1, currentBookings.size());

        LocalDateTime expectedStart = currentBooking.getStart();
        LocalDateTime actualStart = currentBookings.get(0).getStart();

        assertTrue(ChronoUnit.MILLIS.between(expectedStart, actualStart) < 1000);

        LocalDateTime expectedEnd = currentBooking.getEnd();
        LocalDateTime actualEnd = currentBookings.get(0).getEnd();

        assertTrue(ChronoUnit.MILLIS.between(expectedEnd, actualEnd) < 1000);

        assertEquals(currentBooking.getItemId(), currentBookings.get(0).getItem().getId());
    }

    @Test
    void readAllBookerBookings_whenPastState_thenReturnPastBookings() {
        UserDto savedOwner = userService.create(userDto1);

        itemService.create(savedOwner.getId(), itemDtoInput1);
        itemService.create(savedOwner.getId(), itemDtoInput2);

        UserDto savedBooker = userService.create(userDto2);

        bookingService.create(savedBooker.getId(), currentBooking);
        bookingService.create(savedBooker.getId(), pastBooking);
        bookingService.create(savedBooker.getId(), futureBooking);

        List<BookingDtoOutput> pastBookings = new ArrayList<>(bookingService
                .readAllBookerBookings(savedBooker.getId(), "PAST", pageable));

        assertEquals(1, pastBookings.size());
        assertEquals(pastBooking.getStart(), pastBookings.get(0).getStart());
        assertEquals(pastBooking.getEnd(), pastBookings.get(0).getEnd());
        assertEquals(pastBooking.getItemId(), pastBookings.get(0).getItem().getId());
    }

    @Test
    void readAllBookerBookings_whenFutureState_thenReturnFutureBookings() {
        UserDto savedOwner = userService.create(userDto1);

        itemService.create(savedOwner.getId(), itemDtoInput1);
        itemService.create(savedOwner.getId(), itemDtoInput2);

        UserDto savedBooker = userService.create(userDto2);

        bookingService.create(savedBooker.getId(), currentBooking);
        bookingService.create(savedBooker.getId(), pastBooking);
        bookingService.create(savedBooker.getId(), futureBooking);

        List<BookingDtoOutput> futureBookings = new ArrayList<>(bookingService
                .readAllBookerBookings(savedBooker.getId(), "FUTURE", pageable));

        assertEquals(1, futureBookings.size());

        LocalDateTime expectedStart = futureBooking.getStart();
        LocalDateTime actualStart = futureBookings.get(0).getStart();

        assertTrue(ChronoUnit.MILLIS.between(expectedStart, actualStart) < 1000);

        LocalDateTime expectedEnd = futureBooking.getEnd();
        LocalDateTime actualEnd = futureBookings.get(0).getEnd();

        assertTrue(ChronoUnit.MILLIS.between(expectedEnd, actualEnd) < 1000);

        assertEquals(futureBooking.getItemId(), futureBookings.get(0).getItem().getId());
    }

    @Test
    void readAllBookerBookings_whenWaitingState_thenReturnWaitingBookings() {
        UserDto savedOwner = userService.create(userDto1);

        itemService.create(savedOwner.getId(), itemDtoInput1);
        itemService.create(savedOwner.getId(), itemDtoInput2);

        UserDto savedBooker = userService.create(userDto2);

        bookingService.create(savedBooker.getId(), currentBooking);
        bookingService.create(savedBooker.getId(), pastBooking);
        bookingService.create(savedBooker.getId(), futureBooking);
        bookingService.updateApproval(savedOwner.getId(), futureBooking.getId(), false);

        List<BookingDtoOutput> waitingBookings = new ArrayList<>(bookingService
                .readAllBookerBookings(savedBooker.getId(), "WAITING", pageable));

        assertEquals(2, waitingBookings.size());

        LocalDateTime expectedStart0 = currentBooking.getStart();
        LocalDateTime actualStart0 = waitingBookings.get(0).getStart();

        assertTrue(ChronoUnit.MILLIS.between(expectedStart0, actualStart0) < 1000);

        LocalDateTime expectedEnd0 = currentBooking.getEnd();
        LocalDateTime actualEnd0 = waitingBookings.get(0).getEnd();

        assertTrue(ChronoUnit.MILLIS.between(expectedEnd0, actualEnd0) < 1000);

        assertEquals(currentBooking.getItemId(), waitingBookings.get(0).getItem().getId());

        LocalDateTime expectedStart1 = pastBooking.getStart();
        LocalDateTime actualStart1 = waitingBookings.get(1).getStart();

        assertTrue(ChronoUnit.MILLIS.between(expectedStart1, actualStart1) < 1000);

        LocalDateTime expectedEnd1 = pastBooking.getEnd();
        LocalDateTime actualEnd1 = waitingBookings.get(1).getEnd();

        assertTrue(ChronoUnit.MILLIS.between(expectedEnd1, actualEnd1) < 1000);

        assertEquals(pastBooking.getItemId(), waitingBookings.get(1).getItem().getId());
    }

    @Test
    void readAllBookerBookings_whenRejectedState_thenReturnRejectedBookings() {
        UserDto savedOwner = userService.create(userDto1);

        itemService.create(savedOwner.getId(), itemDtoInput1);
        itemService.create(savedOwner.getId(), itemDtoInput2);

        UserDto savedBooker = userService.create(userDto2);

        bookingService.create(savedBooker.getId(), currentBooking);
        bookingService.create(savedBooker.getId(), pastBooking);
        bookingService.create(savedBooker.getId(), futureBooking);
        bookingService.updateApproval(savedOwner.getId(), futureBooking.getId(), false);

        List<BookingDtoOutput> rejectedBookings = new ArrayList<>(bookingService
                .readAllBookerBookings(savedBooker.getId(), "REJECTED", pageable));

        assertEquals(1, rejectedBookings.size());

        LocalDateTime expectedStart = futureBooking.getStart();
        LocalDateTime actualStart = rejectedBookings.get(0).getStart();

        assertTrue(ChronoUnit.MILLIS.between(expectedStart, actualStart) < 1000);

        LocalDateTime expectedEnd = futureBooking.getEnd();
        LocalDateTime actualEnd = rejectedBookings.get(0).getEnd();

        assertTrue(ChronoUnit.MILLIS.between(expectedEnd, actualEnd) < 1000);

        assertEquals(futureBooking.getItemId(), rejectedBookings.get(0).getItem().getId());
    }

    @Test
    void readAllBookerBookings_whenBookerNotFound_thenNotFoundExceptionThrown() {
        Throwable thrown = assertThrows(NotFoundException.class,
                () -> bookingService.readAllBookerBookings(9999, "ALL", pageable));
        assertEquals("Пользователь с id " + 9999 + " не найден", thrown.getMessage());
    }

    @Test
    void readAllOwnerItemBookings_whenAllState_thenReturnAllBookings() {
        UserDto savedOwner = userService.create(userDto1);

        itemService.create(savedOwner.getId(), itemDtoInput1);
        itemService.create(savedOwner.getId(), itemDtoInput2);

        UserDto savedBooker = userService.create(userDto2);

        bookingService.create(savedBooker.getId(), currentBooking);
        bookingService.create(savedBooker.getId(), pastBooking);
        bookingService.create(savedBooker.getId(), futureBooking);

        List<BookingDtoOutput> allBookings = new ArrayList<>(bookingService
                .readAllOwnerItemBookings(savedOwner.getId(), "ALL", pageable));

        assertEquals(3, allBookings.size());
    }

    @Test
    void readAllOwnerItemBookings_whenCurrentState_thenReturnCurrentBookings() {
        UserDto savedOwner = userService.create(userDto1);

        itemService.create(savedOwner.getId(), itemDtoInput1);
        itemService.create(savedOwner.getId(), itemDtoInput2);

        UserDto savedBooker = userService.create(userDto2);

        bookingService.create(savedBooker.getId(), currentBooking);
        bookingService.create(savedBooker.getId(), pastBooking);
        bookingService.create(savedBooker.getId(), futureBooking);

        List<BookingDtoOutput> currentBookings = new ArrayList<>(bookingService
                .readAllOwnerItemBookings(savedOwner.getId(), "CURRENT", pageable));

        assertEquals(1, currentBookings.size());

        LocalDateTime expectedStart = currentBooking.getStart();
        LocalDateTime actualStart = currentBookings.get(0).getStart();

        assertTrue(ChronoUnit.MILLIS.between(expectedStart, actualStart) < 1000);

        LocalDateTime expectedEnd = currentBooking.getEnd();
        LocalDateTime actualEnd = currentBookings.get(0).getEnd();

        assertTrue(ChronoUnit.MILLIS.between(expectedEnd, actualEnd) < 1000);

        assertEquals(currentBooking.getItemId(), currentBookings.get(0).getItem().getId());

    }

    @Test
    void readAllOwnerItemBookings_whenPastState_thenReturnPastBookings() {
        UserDto savedOwner = userService.create(userDto1);

        itemService.create(savedOwner.getId(), itemDtoInput1);
        itemService.create(savedOwner.getId(), itemDtoInput2);

        UserDto savedBooker = userService.create(userDto2);

        bookingService.create(savedBooker.getId(), currentBooking);
        bookingService.create(savedBooker.getId(), pastBooking);
        bookingService.create(savedBooker.getId(), futureBooking);

        List<BookingDtoOutput> pastBookings = new ArrayList<>(bookingService
                .readAllOwnerItemBookings(savedOwner.getId(), "PAST", pageable));

        assertEquals(1, pastBookings.size());
        assertEquals(pastBooking.getStart(), pastBookings.get(0).getStart());
        assertEquals(pastBooking.getEnd(), pastBookings.get(0).getEnd());
        assertEquals(pastBooking.getItemId(), pastBookings.get(0).getItem().getId());
    }

    @Test
    void readAllOwnerItemBookings_whenFutureState_thenReturnFutureBookings() {
        UserDto savedOwner = userService.create(userDto1);

        itemService.create(savedOwner.getId(), itemDtoInput1);
        itemService.create(savedOwner.getId(), itemDtoInput2);

        UserDto savedBooker = userService.create(userDto2);

        bookingService.create(savedBooker.getId(), currentBooking);
        bookingService.create(savedBooker.getId(), pastBooking);
        bookingService.create(savedBooker.getId(), futureBooking);

        List<BookingDtoOutput> futureBookings = new ArrayList<>(bookingService
                .readAllOwnerItemBookings(savedOwner.getId(), "FUTURE", pageable));

        assertEquals(1, futureBookings.size());

        LocalDateTime expectedStart = futureBooking.getStart();
        LocalDateTime actualStart = futureBookings.get(0).getStart();

        assertTrue(ChronoUnit.MILLIS.between(expectedStart, actualStart) < 1000);

        LocalDateTime expectedEnd = futureBooking.getEnd();
        LocalDateTime actualEnd = futureBookings.get(0).getEnd();

        assertTrue(ChronoUnit.MILLIS.between(expectedEnd, actualEnd) < 1000);

        assertEquals(futureBooking.getItemId(), futureBookings.get(0).getItem().getId());
    }

    @Test
    void readAllOwnerItemBookings_whenWaitingState_thenReturnWaitingBookings() {
        UserDto savedOwner = userService.create(userDto1);

        itemService.create(savedOwner.getId(), itemDtoInput1);
        itemService.create(savedOwner.getId(), itemDtoInput2);

        UserDto savedBooker = userService.create(userDto2);

        bookingService.create(savedBooker.getId(), currentBooking);
        bookingService.create(savedBooker.getId(), pastBooking);
        bookingService.create(savedBooker.getId(), futureBooking);
        bookingService.updateApproval(savedOwner.getId(), futureBooking.getId(), false);

        List<BookingDtoOutput> waitingBookings = new ArrayList<>(bookingService
                .readAllOwnerItemBookings(savedOwner.getId(), "WAITING", pageable));

        assertEquals(2, waitingBookings.size());

        LocalDateTime expectedStart0 = currentBooking.getStart();
        LocalDateTime actualStart0 = waitingBookings.get(0).getStart();

        assertTrue(ChronoUnit.MILLIS.between(expectedStart0, actualStart0) < 1000);

        LocalDateTime expectedEnd0 = currentBooking.getEnd();
        LocalDateTime actualEnd0 = waitingBookings.get(0).getEnd();

        assertTrue(ChronoUnit.MILLIS.between(expectedEnd0, actualEnd0) < 1000);

        assertEquals(currentBooking.getItemId(), waitingBookings.get(0).getItem().getId());

        LocalDateTime expectedStart1 = pastBooking.getStart();
        LocalDateTime actualStart1 = waitingBookings.get(1).getStart();

        assertTrue(ChronoUnit.MILLIS.between(expectedStart1, actualStart1) < 1000);

        LocalDateTime expectedEnd1 = pastBooking.getEnd();
        LocalDateTime actualEnd1 = waitingBookings.get(1).getEnd();

        assertTrue(ChronoUnit.MILLIS.between(expectedEnd1, actualEnd1) < 1000);

        assertEquals(pastBooking.getItemId(), waitingBookings.get(1).getItem().getId());
    }

    @Test
    void readAllOwnerItemBookings_whenRejectedState_thenReturnRejectedBookings() {
        UserDto savedOwner = userService.create(userDto1);

        itemService.create(savedOwner.getId(), itemDtoInput1);
        itemService.create(savedOwner.getId(), itemDtoInput2);

        UserDto savedBooker = userService.create(userDto2);

        bookingService.create(savedBooker.getId(), currentBooking);
        bookingService.create(savedBooker.getId(), pastBooking);
        bookingService.create(savedBooker.getId(), futureBooking);
        bookingService.updateApproval(savedOwner.getId(), futureBooking.getId(), false);

        List<BookingDtoOutput> rejectedBookings = new ArrayList<>(bookingService
                .readAllOwnerItemBookings(savedOwner.getId(), "REJECTED", pageable));

        assertEquals(1, rejectedBookings.size());

        LocalDateTime expectedStart = futureBooking.getStart();
        LocalDateTime actualStart = rejectedBookings.get(0).getStart();

        assertTrue(ChronoUnit.MILLIS.between(expectedStart, actualStart) < 1000);

        LocalDateTime expectedEnd = futureBooking.getEnd();
        LocalDateTime actualEnd = rejectedBookings.get(0).getEnd();

        assertTrue(ChronoUnit.MILLIS.between(expectedEnd, actualEnd) < 1000);

        assertEquals(futureBooking.getItemId(), rejectedBookings.get(0).getItem().getId());
    }

    @Test
    void readAllOwnerItemBookings_whenOwnerNotFound_thenNotFoundExceptionThrown() {
        Throwable thrown = assertThrows(NotFoundException.class,
                () -> bookingService.readAllOwnerItemBookings(9999, "ALL", pageable));
        assertEquals("Пользователь с id " + 9999 + " не найден", thrown.getMessage());
    }

    @Test
    void updateApproval_whenBookingAvailable_thenApprovedBooking() {
        UserDto savedOwner = userService.create(userDto1);
        UserDto savedBooker = userService.create(userDto2);

        itemService.create(savedOwner.getId(), itemDtoInput1);
        bookingService.create(savedBooker.getId(), currentBooking);

        BookingDtoOutput updatedBooking = bookingService.updateApproval(savedOwner.getId(),
                currentBooking.getId(), true);

        assertEquals(BookingApproval.APPROVED, updatedBooking.getStatus());
    }

    @Test
    void updateApproval_whenBookingNotAvailable_thenRejectedBooking() {
        UserDto savedOwner = userService.create(userDto1);
        UserDto savedBooker = userService.create(userDto2);

        itemService.create(savedOwner.getId(), itemDtoInput1);
        bookingService.create(savedBooker.getId(), currentBooking);

        BookingDtoOutput updatedBooking = bookingService.updateApproval(savedOwner.getId(),
                currentBooking.getId(), false);

        assertEquals(BookingApproval.REJECTED, updatedBooking.getStatus());
    }

    @Test
    void updateApproval_whenOwnerNotFound_thenNotFoundExceptionThrown() {
        Throwable thrown = assertThrows(NotFoundException.class,
                () -> bookingService.updateApproval(999, currentBooking.getId(), false));
        assertEquals("Пользователь с id " + 999 + " не найден", thrown.getMessage());
    }

    @Test
    void updateApproval_whenBookingNotFound_thenNotFoundExceptionThrown() {
        UserDto savedOwner = userService.create(userDto1);

        Throwable thrown = assertThrows(NotFoundException.class,
                () -> bookingService.updateApproval(savedOwner.getId(), 999, false));
        assertEquals("Бронирование с id " + 999 + " не найдено", thrown.getMessage());
    }

    @Test
    void updateApproval_whenUserNotOwner_thenNotFoundExceptionThrown() {
        UserDto savedOwner = userService.create(userDto1);
        UserDto savedBooker = userService.create(userDto2);

        itemService.create(savedOwner.getId(), itemDtoInput1);
        bookingService.create(savedBooker.getId(), currentBooking);

        Throwable thrown = assertThrows(NotFoundException.class,
                () -> bookingService.updateApproval(savedBooker.getId(), currentBooking.getId(), false));
        assertEquals("Пользователь с id " + savedBooker.getId()
                + " не может редактировать бронирование с id " + currentBooking.getId(), thrown.getMessage());
    }

    @Test
    void updateApproval_whenBookingApproved_thenItemAvailabilityExceptionThrown() {
        UserDto savedOwner = userService.create(userDto1);
        UserDto savedBooker = userService.create(userDto2);

        itemService.create(savedOwner.getId(), itemDtoInput1);
        bookingService.create(savedBooker.getId(), currentBooking);

        bookingService.updateApproval(savedOwner.getId(), currentBooking.getId(), true);

        Throwable thrown = assertThrows(ItemAvailabilityException.class,
                () -> bookingService.updateApproval(savedOwner.getId(), currentBooking.getId(), true));
        assertEquals("Бронирование уже подтверждено", thrown.getMessage());

        thrown = assertThrows(ItemAvailabilityException.class,
                () -> bookingService.updateApproval(savedOwner.getId(), currentBooking.getId(), false));
        assertEquals("Бронирование уже подтверждено", thrown.getMessage());
    }

}