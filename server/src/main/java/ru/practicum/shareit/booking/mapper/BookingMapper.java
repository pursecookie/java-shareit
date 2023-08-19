package ru.practicum.shareit.booking.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingDtoForOwner;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingApproval;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

@UtilityClass
public class BookingMapper {
    public Booking mapToBooking(BookingDtoInput bookingDtoInput, Item item, User booker, BookingApproval status) {
        return new Booking(bookingDtoInput.getId(),
                bookingDtoInput.getStart(),
                bookingDtoInput.getEnd(),
                item,
                booker,
                status);
    }

    public BookingDtoOutput mapToBookingDtoOutput(Booking booking) {
        return new BookingDtoOutput(booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                ItemMapper.mapToItemDtoOutput(booking.getItem(), null, null),
                UserMapper.mapToUserDto(booking.getBooker()),
                booking.getStatus());
    }

    public BookingDtoForOwner maptoBookingDtoForOwner(Booking booking) {
        if (booking == null) {
            return null;
        }

        return new BookingDtoForOwner(booking.getId(),
                booking.getBooker().getId());
    }

}