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
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingApproval;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.ItemAvailabilityException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDtoInput;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class BookingServiceUnitTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private BookingServiceImpl bookingService;
    User user1;
    User user2;
    UserDto userDto1;
    UserDto userDto2;
    Item item1;
    Item item2;
    ItemDtoInput itemDtoInput1;
    ItemDtoInput itemDtoInput2;
    Booking booking1;
    Booking booking2;
    Booking booking3;
    BookingDtoInput currentBooking;
    BookingDtoInput pastBooking;
    BookingDtoInput futureBooking;
    Pageable pageable;

    @BeforeEach
    void setUp() {
        userDto1 = new UserDto(1, "User1", "user1@mail.ru");
        user1 = UserMapper.mapToUser(userDto1);

        userDto2 = new UserDto(2, "User2", "user2@mail.ru");
        user2 = UserMapper.mapToUser(userDto2);

        itemDtoInput1 = new ItemDtoInput(1, "Item1", "Item1 Description", true, 0);
        item1 = ItemMapper.mapToItem(itemDtoInput1, user1, null);

        itemDtoInput2 = new ItemDtoInput(2, "Item2", "Item2 Description", true, 0);
        item2 = ItemMapper.mapToItem(itemDtoInput2, user1, null);

        currentBooking = new BookingDtoInput(1, LocalDateTime.now(), LocalDateTime.now().plusHours(2), 1);
        booking1 = BookingMapper.mapToBooking(currentBooking, item1, user2, BookingApproval.WAITING);

        pastBooking = new BookingDtoInput(2,
                LocalDateTime.of(2023, 8, 1, 10, 0, 0),
                LocalDateTime.of(2023, 8, 8, 10, 0, 0),
                1);
        booking2 = BookingMapper.mapToBooking(pastBooking, item1, user2, BookingApproval.WAITING);

        futureBooking = new BookingDtoInput(3, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2), 2);
        booking3 = BookingMapper.mapToBooking(futureBooking, item2, user2, BookingApproval.REJECTED);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void create_whenAllIsOk_thenReturnedBooking() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        Mockito.when(bookingRepository.save(Mockito.any())).thenReturn(booking1);

        BookingDtoOutput createdBooking = bookingService.create(2, currentBooking);

        Mockito.verify(bookingRepository).save(Mockito.any());

        assertEquals(currentBooking.getStart(), createdBooking.getStart());
        assertEquals(currentBooking.getEnd(), createdBooking.getEnd());
        assertEquals(currentBooking.getItemId(), createdBooking.getItem().getId());
    }

    @Test
    void create_whenBookerNotFound_thenNotFoundExceptionThrown() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.create(999, currentBooking));

        assertEquals("Пользователь с id " + 999 + " не найден", notFoundException.getMessage());
    }

    @Test
    void create_whenItemNotFound_thenNotFoundExceptionThrown() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findById(999L)).thenReturn(Optional.empty());
        currentBooking.setItemId(999);

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.create(2, currentBooking));

        assertEquals("Вещь с id " + currentBooking.getItemId() + " не найдена",
                notFoundException.getMessage());
    }

    @Test
    void create_whenBookerIsOwner_thenNotFoundExceptionThrown() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.create(1, currentBooking));

        assertEquals("Владелец вещи не может забронировать свою же вещь", notFoundException.getMessage());
    }

    @Test
    void create_whenItemUnavailable_thenItemAvailabilityExceptionThrown() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        item1.setAvailable(false);
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));

        ItemAvailabilityException itemAvailabilityException = assertThrows(ItemAvailabilityException.class,
                () -> bookingService.create(2, currentBooking));

        assertEquals("Вещь с id " + currentBooking.getItemId() + " недоступна к бронированию",
                itemAvailabilityException.getMessage());
    }

    @Test
    void read_whenAllIsOk_thenReturnedBooking() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking1));

        BookingDtoOutput returnedBooking = bookingService.read(2, 1);

        Mockito.verify(bookingRepository).findById(1L);

        assertEquals(currentBooking.getStart(), returnedBooking.getStart());
        assertEquals(currentBooking.getEnd(), returnedBooking.getEnd());
        assertEquals(currentBooking.getItemId(), returnedBooking.getItem().getId());
    }

    @Test
    void read_whenUserNotFound_thenNotFoundExceptionThrown() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.read(999, 1));

        assertEquals("Пользователь с id " + 999 + " не найден", notFoundException.getMessage());
    }

    @Test
    void read_whenBookingNotFound_thenNotFoundExceptionThrown() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.read(2, 999));

        assertEquals("Бронирование с id " + 999 + " не найдено", notFoundException.getMessage());
    }

    @Test
    void read_whenUserNotOwnerOrBooker_thenNotFoundExceptionThrown() {
        Mockito.when(userRepository.findById(3L))
                .thenReturn(Optional.of(new User(3, "User3", "user3@mail.ru")));
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking1));

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.read(3, 1));

        assertEquals("Бронирование с id " + 1 + " не найдено для пользователя с id " + 3,
                notFoundException.getMessage());
    }

    @Test
    void readAllBookerBookings_whenAllState_thenReturnAllBookings() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(bookingRepository.findAllByBooker_IdOrderByStartDesc(pageable, 2))
                .thenReturn(new PageImpl<>(Arrays.asList(booking1, booking2, booking3)));

        List<BookingDtoOutput> allBookings = new ArrayList<>(bookingService
                .readAllBookerBookings(2, "ALL", pageable));

        Mockito.verify(bookingRepository).findAllByBooker_IdOrderByStartDesc(pageable, 2);

        assertEquals(3, allBookings.size());
    }

    @Test
    void readAllBookerBookings_whenCurrentState_thenReturnCurrentBookings() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(bookingRepository.readAllBookerCurrentBookings(Mockito.any(), Mockito.anyLong(), Mockito.any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(booking1)));

        List<BookingDtoOutput> currentBookings = new ArrayList<>(bookingService
                .readAllBookerBookings(2, "CURRENT", pageable));

        Mockito.verify(bookingRepository).readAllBookerCurrentBookings(Mockito.any(), Mockito.anyLong(), Mockito.any());

        assertEquals(1, currentBookings.size());
        assertEquals(currentBooking.getStart(), currentBookings.get(0).getStart());
        assertEquals(currentBooking.getEnd(), currentBookings.get(0).getEnd());
        assertEquals(currentBooking.getItemId(), currentBookings.get(0).getItem().getId());
    }

    @Test
    void readAllBookerBookings_whenPastState_thenReturnPastBookings() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(bookingRepository.readAllBookerPastBookings(Mockito.any(), Mockito.anyLong(), Mockito.any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(booking2)));

        List<BookingDtoOutput> pastBookings = new ArrayList<>(bookingService
                .readAllBookerBookings(2, "PAST", pageable));

        Mockito.verify(bookingRepository).readAllBookerPastBookings(Mockito.any(), Mockito.anyLong(), Mockito.any());

        assertEquals(1, pastBookings.size());
        assertEquals(pastBooking.getStart(), pastBookings.get(0).getStart());
        assertEquals(pastBooking.getEnd(), pastBookings.get(0).getEnd());
        assertEquals(pastBooking.getItemId(), pastBookings.get(0).getItem().getId());
    }

    @Test
    void readAllBookerBookings_whenFutureState_thenReturnFutureBookings() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(bookingRepository.readAllBookerFutureBookings(Mockito.any(), Mockito.anyLong(), Mockito.any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(booking3)));

        List<BookingDtoOutput> futureBookings = new ArrayList<>(bookingService
                .readAllBookerBookings(2, "FUTURE", pageable));

        Mockito.verify(bookingRepository).readAllBookerFutureBookings(Mockito.any(), Mockito.anyLong(), Mockito.any());

        assertEquals(1, futureBookings.size());
        assertEquals(futureBooking.getStart(), futureBookings.get(0).getStart());
        assertEquals(futureBooking.getEnd(), futureBookings.get(0).getEnd());
        assertEquals(futureBooking.getItemId(), futureBookings.get(0).getItem().getId());
    }

    @Test
    void readAllBookerBookings_whenWaitingState_thenReturnWaitingBookings() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(bookingRepository
                        .findAllByBooker_IdAndStatusInOrderByStartDesc(Mockito.any(), Mockito.anyLong(), Mockito.any()))
                .thenReturn(new PageImpl<>(Arrays.asList(booking1, booking2)));

        List<BookingDtoOutput> waitingBookings = new ArrayList<>(bookingService
                .readAllBookerBookings(2, "WAITING", pageable));

        Mockito.verify(bookingRepository)
                .findAllByBooker_IdAndStatusInOrderByStartDesc(Mockito.any(), Mockito.anyLong(), Mockito.any());

        assertEquals(2, waitingBookings.size());

        assertEquals(currentBooking.getStart(), waitingBookings.get(0).getStart());
        assertEquals(currentBooking.getEnd(), waitingBookings.get(0).getEnd());
        assertEquals(currentBooking.getItemId(), waitingBookings.get(0).getItem().getId());

        assertEquals(pastBooking.getStart(), waitingBookings.get(1).getStart());
        assertEquals(pastBooking.getEnd(), waitingBookings.get(1).getEnd());
        assertEquals(pastBooking.getItemId(), waitingBookings.get(1).getItem().getId());
    }

    @Test
    void readAllBookerBookings_whenRejectedState_thenReturnRejectedBookings() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(bookingRepository
                        .findAllByBooker_IdAndStatusInOrderByStartDesc(Mockito.any(), Mockito.anyLong(), Mockito.any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(booking3)));

        List<BookingDtoOutput> rejectedBookings = new ArrayList<>(bookingService
                .readAllBookerBookings(2, "REJECTED", pageable));

        Mockito.verify(bookingRepository)
                .findAllByBooker_IdAndStatusInOrderByStartDesc(Mockito.any(), Mockito.anyLong(), Mockito.any());

        assertEquals(1, rejectedBookings.size());
        assertEquals(futureBooking.getStart(), rejectedBookings.get(0).getStart());
        assertEquals(futureBooking.getEnd(), rejectedBookings.get(0).getEnd());
        assertEquals(futureBooking.getItemId(), rejectedBookings.get(0).getItem().getId());
    }

    @Test
    void readAllBookerBookings_whenBookerNotFound_thenNotFoundExceptionThrown() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.readAllBookerBookings(999, "ALL", pageable));

        assertEquals("Пользователь с id " + 999 + " не найден", notFoundException.getMessage());
    }

    @Test
    void readAllOwnerItemBookings_whenAllState_thenReturnAllBookings() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user1));
        Mockito.when(itemRepository.findAllByOwnerId(1L))
                .thenReturn(Arrays.asList(item1, item2));
        Mockito.when(bookingRepository.findAllByItem_IdInOrderByStartDesc(Mockito.any(), Mockito.anyList()))
                .thenReturn(new PageImpl<>(Arrays.asList(booking1, booking2, booking3)));

        List<BookingDtoOutput> allBookings = new ArrayList<>(bookingService
                .readAllOwnerItemBookings(1, "ALL", pageable));

        Mockito.verify(bookingRepository).findAllByItem_IdInOrderByStartDesc(Mockito.any(), Mockito.anyList());

        assertEquals(3, allBookings.size());
    }

    @Test
    void readAllOwnerItemBookings_whenCurrentState_thenReturnCurrentBookings() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user1));
        Mockito.when(itemRepository.findAllByOwnerId(1L))
                .thenReturn(Arrays.asList(item1, item2));
        Mockito.when(bookingRepository
                        .readAllOwnerItemsCurrentBookings(Mockito.any(), Mockito.anyList(), Mockito.any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(booking1)));

        List<BookingDtoOutput> currentBookings = new ArrayList<>(bookingService
                .readAllOwnerItemBookings(1, "CURRENT", pageable));

        Mockito.verify(bookingRepository)
                .readAllOwnerItemsCurrentBookings(Mockito.any(), Mockito.anyList(), Mockito.any());

        assertEquals(1, currentBookings.size());
        assertEquals(currentBooking.getStart(), currentBookings.get(0).getStart());
        assertEquals(currentBooking.getEnd(), currentBookings.get(0).getEnd());
        assertEquals(currentBooking.getItemId(), currentBookings.get(0).getItem().getId());
    }

    @Test
    void readAllOwnerItemBookings_whenPastState_thenReturnPastBookings() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user1));
        Mockito.when(itemRepository.findAllByOwnerId(1L))
                .thenReturn(Arrays.asList(item1, item2));
        Mockito.when(bookingRepository
                        .readAllOwnerItemsPastBookings(Mockito.any(), Mockito.anyList(), Mockito.any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(booking2)));

        List<BookingDtoOutput> pastBookings = new ArrayList<>(bookingService
                .readAllOwnerItemBookings(1, "PAST", pageable));

        Mockito.verify(bookingRepository)
                .readAllOwnerItemsPastBookings(Mockito.any(), Mockito.anyList(), Mockito.any());

        assertEquals(1, pastBookings.size());
        assertEquals(pastBooking.getStart(), pastBookings.get(0).getStart());
        assertEquals(pastBooking.getEnd(), pastBookings.get(0).getEnd());
        assertEquals(pastBooking.getItemId(), pastBookings.get(0).getItem().getId());
    }

    @Test
    void readAllOwnerItemBookings_whenFutureState_thenReturnFutureBookings() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user1));
        Mockito.when(itemRepository.findAllByOwnerId(1L))
                .thenReturn(Arrays.asList(item1, item2));
        Mockito.when(bookingRepository
                        .readAllOwnerItemsFutureBookings(Mockito.any(), Mockito.anyList(), Mockito.any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(booking3)));

        List<BookingDtoOutput> futureBookings = new ArrayList<>(bookingService
                .readAllOwnerItemBookings(1, "FUTURE", pageable));

        Mockito.verify(bookingRepository)
                .readAllOwnerItemsFutureBookings(Mockito.any(), Mockito.anyList(), Mockito.any());

        assertEquals(1, futureBookings.size());
        assertEquals(futureBooking.getStart(), futureBookings.get(0).getStart());
        assertEquals(futureBooking.getEnd(), futureBookings.get(0).getEnd());
        assertEquals(futureBooking.getItemId(), futureBookings.get(0).getItem().getId());
    }

    @Test
    void readAllOwnerItemBookings_whenWaitingState_thenReturnWaitingBookings() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user1));
        Mockito.when(itemRepository.findAllByOwnerId(1L))
                .thenReturn(Arrays.asList(item1, item2));
        Mockito.when(bookingRepository
                        .findAllByItem_IdInAndStatusInOrderByStartDesc(Mockito.any(), Mockito.anyList(), Mockito.any()))
                .thenReturn(new PageImpl<>(Arrays.asList(booking1, booking2)));

        List<BookingDtoOutput> waitingBookings = new ArrayList<>(bookingService
                .readAllOwnerItemBookings(1, "WAITING", pageable));

        Mockito.verify(bookingRepository)
                .findAllByItem_IdInAndStatusInOrderByStartDesc(Mockito.any(), Mockito.anyList(), Mockito.any());

        assertEquals(2, waitingBookings.size());

        assertEquals(currentBooking.getStart(), waitingBookings.get(0).getStart());
        assertEquals(currentBooking.getEnd(), waitingBookings.get(0).getEnd());
        assertEquals(currentBooking.getItemId(), waitingBookings.get(0).getItem().getId());

        assertEquals(pastBooking.getStart(), waitingBookings.get(1).getStart());
        assertEquals(pastBooking.getEnd(), waitingBookings.get(1).getEnd());
        assertEquals(pastBooking.getItemId(), waitingBookings.get(1).getItem().getId());
    }

    @Test
    void readAllOwnerItemBookings_whenRejectedState_thenReturnRejectedBookings() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user1));
        Mockito.when(itemRepository.findAllByOwnerId(1L))
                .thenReturn(Arrays.asList(item1, item2));
        Mockito.when(bookingRepository
                        .findAllByItem_IdInAndStatusInOrderByStartDesc(Mockito.any(), Mockito.anyList(), Mockito.any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(booking3)));

        List<BookingDtoOutput> rejectedBookings = new ArrayList<>(bookingService
                .readAllOwnerItemBookings(1, "WAITING", pageable));

        Mockito.verify(bookingRepository)
                .findAllByItem_IdInAndStatusInOrderByStartDesc(Mockito.any(), Mockito.anyList(), Mockito.any());

        assertEquals(1, rejectedBookings.size());
        assertEquals(futureBooking.getStart(), rejectedBookings.get(0).getStart());
        assertEquals(futureBooking.getEnd(), rejectedBookings.get(0).getEnd());
        assertEquals(futureBooking.getItemId(), rejectedBookings.get(0).getItem().getId());
    }

    @Test
    void readAllOwnerItemBookings_whenOwnerNotFound_thenNotFoundExceptionThrown() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.readAllOwnerItemBookings(999, "ALL", pageable));

        assertEquals("Пользователь с id " + 999 + " не найден", notFoundException.getMessage());
    }

    @Test
    void updateApproval_whenBookingAvailable_thenApprovedBooking() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking1));
        Mockito.when(bookingRepository.save(Mockito.any()))
                .thenReturn(BookingMapper.mapToBooking(currentBooking, item1, user2, BookingApproval.APPROVED));

        BookingDtoOutput updatedBooking = bookingService.updateApproval(1, 1, true);

        Mockito.verify(bookingRepository).save(Mockito.any());

        assertEquals(BookingApproval.APPROVED, updatedBooking.getStatus());
    }

    @Test
    void updateApproval_whenBookingNotAvailable_thenRejectedBooking() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking1));
        Mockito.when(bookingRepository.save(Mockito.any()))
                .thenReturn(BookingMapper.mapToBooking(currentBooking, item1, user2, BookingApproval.REJECTED));

        BookingDtoOutput updatedBooking = bookingService.updateApproval(1, 1, false);

        Mockito.verify(bookingRepository).save(Mockito.any());

        assertEquals(BookingApproval.REJECTED, updatedBooking.getStatus());
    }

    @Test
    void updateApproval_whenOwnerNotFound_thenNotFoundExceptionThrown() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.updateApproval(999, 1, true));

        assertEquals("Пользователь с id " + 999 + " не найден", notFoundException.getMessage());
    }

    @Test
    void updateApproval_whenBookingNotFound_thenNotFoundExceptionThrown() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        Mockito.when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.updateApproval(1, 999, true));

        assertEquals("Бронирование с id " + 999 + " не найдено", notFoundException.getMessage());
    }

    @Test
    void updateApproval_whenUserNotOwner_thenNotFoundExceptionThrown() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking1));

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.updateApproval(2, 1, true));

        assertEquals("Пользователь с id " + 2 + " не может редактировать бронирование с id " + 1,
                notFoundException.getMessage());
    }

    @Test
    void updateApproval_whenBookingApproved_thenItemAvailabilityExceptionThrown() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        booking1.setStatus(BookingApproval.APPROVED);

        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking1));

        ItemAvailabilityException itemAvailabilityException = assertThrows(ItemAvailabilityException.class,
                () -> bookingService.updateApproval(1, 1, true));

        assertEquals("Бронирование уже подтверждено", itemAvailabilityException.getMessage());

        itemAvailabilityException = assertThrows(ItemAvailabilityException.class,
                () -> bookingService.updateApproval(1, 1, false));

        assertEquals("Бронирование уже подтверждено", itemAvailabilityException.getMessage());
    }
}