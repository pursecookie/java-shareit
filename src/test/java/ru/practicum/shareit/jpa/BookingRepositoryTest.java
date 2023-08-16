package ru.practicum.shareit.jpa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingApproval;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class BookingRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private BookingRepository bookingRepository;
    User owner;
    User booker;
    Item item;
    Booking booking;
    Pageable pageable;

    @BeforeEach
    void setUp() {
        owner = new User();

        owner.setName("User1");
        owner.setEmail("user1@mail.ru");

        userRepository.save(owner);

        item = new Item();

        item.setName("Item1");
        item.setDescription("Item1 Description");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setItemRequest(null);

        itemRepository.save(item);

        booker = new User();

        booker.setName("User2");
        booker.setEmail("user2@mail.ru");

        userRepository.save(booker);

        booking = new Booking();

        booking.setStart(LocalDateTime.now().plusHours(1));
        booking.setEnd(LocalDateTime.now().plusHours(2));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingApproval.WAITING);

        bookingRepository.save(booking);

        pageable = PageRequest.of(0, 10);
    }

    @AfterEach
    void deleteItems() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void readAllBookerCurrentBookings() {
        booking.setStart(LocalDateTime.now().minusHours(2));
        bookingRepository.save(booking);

        Page<Booking> currentBookings = bookingRepository
                .readAllBookerCurrentBookings(pageable, booker.getId(), LocalDateTime.now());

        assertEquals(1, currentBookings.getContent().size());
        assertEquals(booking.getStart(), currentBookings.getContent().get(0).getStart());
        assertEquals(booking.getEnd(), currentBookings.getContent().get(0).getEnd());
        assertEquals(booking.getItem().getId(), currentBookings.getContent().get(0).getItem().getId());
        assertEquals(booking.getBooker().getId(), currentBookings.getContent().get(0).getBooker().getId());
        assertEquals(booking.getStatus(), currentBookings.getContent().get(0).getStatus());
    }

    @Test
    void readAllBookerPastBookings() {
        booking.setStart(LocalDateTime.now().minusHours(3));
        booking.setEnd(LocalDateTime.now().minusHours(3));
        bookingRepository.save(booking);

        Page<Booking> pastBookings = bookingRepository
                .readAllBookerPastBookings(pageable, booker.getId(), LocalDateTime.now());

        assertEquals(1, pastBookings.getContent().size());
        assertEquals(booking.getStart(), pastBookings.getContent().get(0).getStart());
        assertEquals(booking.getEnd(), pastBookings.getContent().get(0).getEnd());
        assertEquals(booking.getItem().getId(), pastBookings.getContent().get(0).getItem().getId());
        assertEquals(booking.getBooker().getId(), pastBookings.getContent().get(0).getBooker().getId());
        assertEquals(booking.getStatus(), pastBookings.getContent().get(0).getStatus());
    }

    @Test
    void readAllBookerFutureBookings() {
        Page<Booking> futureBookings = bookingRepository
                .readAllBookerFutureBookings(pageable, booker.getId(), LocalDateTime.now());

        assertEquals(1, futureBookings.getContent().size());
        assertEquals(booking.getStart(), futureBookings.getContent().get(0).getStart());
        assertEquals(booking.getEnd(), futureBookings.getContent().get(0).getEnd());
        assertEquals(booking.getItem().getId(), futureBookings.getContent().get(0).getItem().getId());
        assertEquals(booking.getBooker().getId(), futureBookings.getContent().get(0).getBooker().getId());
        assertEquals(booking.getStatus(), futureBookings.getContent().get(0).getStatus());
    }

    @Test
    void readAllOwnerItemsCurrentBookings() {
        booking.setStart(LocalDateTime.now().minusHours(2));
        bookingRepository.save(booking);

        Page<Booking> currentBookings = bookingRepository
                .readAllOwnerItemsCurrentBookings(pageable, List.of(item.getId()), LocalDateTime.now());

        assertEquals(1, currentBookings.getContent().size());
        assertEquals(booking.getStart(), currentBookings.getContent().get(0).getStart());
        assertEquals(booking.getEnd(), currentBookings.getContent().get(0).getEnd());
        assertEquals(booking.getItem().getId(), currentBookings.getContent().get(0).getItem().getId());
        assertEquals(booking.getBooker().getId(), currentBookings.getContent().get(0).getBooker().getId());
        assertEquals(booking.getStatus(), currentBookings.getContent().get(0).getStatus());
    }

    @Test
    void readAllOwnerItemsPastBookings() {
        booking.setStart(LocalDateTime.now().minusHours(3));
        booking.setEnd(LocalDateTime.now().minusHours(3));
        bookingRepository.save(booking);

        Page<Booking> pastBookings = bookingRepository
                .readAllOwnerItemsPastBookings(pageable, List.of(item.getId()), LocalDateTime.now());

        assertEquals(1, pastBookings.getContent().size());
        assertEquals(booking.getStart(), pastBookings.getContent().get(0).getStart());
        assertEquals(booking.getEnd(), pastBookings.getContent().get(0).getEnd());
        assertEquals(booking.getItem().getId(), pastBookings.getContent().get(0).getItem().getId());
        assertEquals(booking.getBooker().getId(), pastBookings.getContent().get(0).getBooker().getId());
        assertEquals(booking.getStatus(), pastBookings.getContent().get(0).getStatus());
    }

    @Test
    void readAllOwnerItemsFutureBookings() {
        Page<Booking> futureBookings = bookingRepository
                .readAllOwnerItemsFutureBookings(pageable, List.of(item.getId()), LocalDateTime.now());

        assertEquals(1, futureBookings.getContent().size());
        assertEquals(booking.getStart(), futureBookings.getContent().get(0).getStart());
        assertEquals(booking.getEnd(), futureBookings.getContent().get(0).getEnd());
        assertEquals(booking.getItem().getId(), futureBookings.getContent().get(0).getItem().getId());
        assertEquals(booking.getBooker().getId(), futureBookings.getContent().get(0).getBooker().getId());
        assertEquals(booking.getStatus(), futureBookings.getContent().get(0).getStatus());
    }

}