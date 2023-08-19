package ru.practicum.shareit.booking.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;

import java.util.Collection;

public interface BookingService {
    BookingDtoOutput create(long bookerId, BookingDtoInput bookingDtoInput);

    BookingDtoOutput read(long userId, long id);

    Collection<BookingDtoOutput> readAllBookerBookings(long bookerId, String state, Pageable pageable);

    Collection<BookingDtoOutput> readAllOwnerItemBookings(long ownerId, String state, Pageable pageable);

    BookingDtoOutput updateApproval(long ownerId, long id, Boolean isApproved);
}