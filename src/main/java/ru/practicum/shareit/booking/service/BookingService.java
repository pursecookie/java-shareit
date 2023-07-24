package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;

import java.util.Collection;

public interface BookingService {
    BookingDtoOutput create(long bookerId, BookingDtoInput bookingDtoInput);

    BookingDtoOutput read(long userId, long id);

    Collection<BookingDtoOutput> readAllBookerBookings(long bookerId, String state);

    Collection<BookingDtoOutput> readAllOwnerItemBookings(long ownerId, String state);

    BookingDtoOutput updateApproval(long ownerId, long id, Boolean isApproved);
}