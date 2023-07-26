package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingApproval;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ItemAvailabilityException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingDtoOutput create(long bookerId, BookingDtoInput bookingDtoInput) {
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + bookerId + " не найден"));

        Item item = itemRepository.findById(bookingDtoInput.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с id " + bookingDtoInput.getItemId() + " не найдена"));

        if (item.getOwner().getId() == bookerId) {
            throw new NotFoundException("Владелец вещи не может забронировать свою же вещь");
        }

        if (!item.getAvailable()) {
            throw new ItemAvailabilityException("Вещь с id " + bookingDtoInput.getItemId()
                    + " недоступна к бронированию");
        }

        Booking booking = BookingMapper.mapToBooking(bookingDtoInput, item, booker, BookingApproval.WAITING);

        return BookingMapper.mapToBookingDtoOutput(bookingRepository.save(booking));
    }

    @Override
    public BookingDtoOutput read(long userId, long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Бронирование с id " + id + " не найдено"));

        long bookerId = booking.getBooker().getId();
        long ownerId = booking.getItem().getOwner().getId();

        if (bookerId != userId && ownerId != userId) {
            throw new NotFoundException("Бронирование с id " + id + " не найдено для пользователя с id " + userId);
        }

        return BookingMapper.mapToBookingDtoOutput(booking);
    }

    @Override
    public Collection<BookingDtoOutput> readAllBookerBookings(long bookerId, String state) {
        userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + bookerId + " не найден"));

        switch (state) {
            case "CURRENT":
                return bookingRepository.readAllBookerCurrentBookings(bookerId, LocalDateTime.now())
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "PAST":
                return bookingRepository.readAllBookerPastBookings(bookerId, LocalDateTime.now())
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "FUTURE":
                return bookingRepository.readAllBookerFutureBookings(bookerId, LocalDateTime.now())
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "WAITING":
                return bookingRepository
                        .findAllByBooker_IdAndStatusInOrderByStartDesc(bookerId, List.of(BookingApproval.WAITING))
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "REJECTED":
                return bookingRepository
                        .findAllByBooker_IdAndStatusInOrderByStartDesc(bookerId, List.of(BookingApproval.REJECTED))
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            default:
                return bookingRepository.findAllByBooker_IdOrderByStartDesc(bookerId)
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
        }
    }

    @Override
    public Collection<BookingDtoOutput> readAllOwnerItemBookings(long ownerId, String state) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + ownerId + " не найден"));

        List<Long> userItems = itemRepository.findAllByOwnerId(ownerId)
                .stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        switch (state) {
            case "CURRENT":
                return bookingRepository.readAllOwnerItemsCurrentBookings(userItems, LocalDateTime.now())
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "PAST":
                return bookingRepository.readAllOwnerItemsPastBookings(userItems, LocalDateTime.now())
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "FUTURE":
                return bookingRepository.readAllOwnerItemsFutureBookings(userItems, LocalDateTime.now())
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "WAITING":
                return bookingRepository
                        .findAllByItem_IdInAndStatusInOrderByStartDesc(userItems, List.of(BookingApproval.WAITING))
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "REJECTED":
                return bookingRepository
                        .findAllByItem_IdInAndStatusInOrderByStartDesc(userItems, List.of(BookingApproval.REJECTED))
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            default:
                return bookingRepository.findAllByItem_IdInOrderByStartDesc(userItems)
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
        }
    }

    @Override
    public BookingDtoOutput updateApproval(long ownerId, long id, Boolean isApproved) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Бронирование с id " + id + " не найдено"));

        if (booking.getItem().getOwner().getId() != ownerId) {
            throw new NotFoundException("Пользователь с id " + ownerId
                    + " не может редактировать бронирование с id " + id);
        }

        if (booking.getStatus().equals(BookingApproval.APPROVED)) {
            throw new ItemAvailabilityException("Бронирование уже подтверждено");
        }

        if (isApproved) {
            booking.setStatus(BookingApproval.APPROVED);
        } else {
            booking.setStatus(BookingApproval.REJECTED);
        }

        return BookingMapper.mapToBookingDtoOutput(bookingRepository.save(booking));
    }
}