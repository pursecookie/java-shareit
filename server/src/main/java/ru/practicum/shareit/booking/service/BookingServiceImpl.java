package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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

import javax.transaction.Transactional;
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
    @Transactional
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
    @Transactional
    public BookingDtoOutput read(long userId, long id) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

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
    @Transactional
    public Collection<BookingDtoOutput> readAllBookerBookings(long bookerId, String state, Pageable pageable) {
        userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + bookerId + " не найден"));

        switch (state) {
            case "CURRENT":
                return bookingRepository.readAllBookerCurrentBookings(pageable, bookerId, LocalDateTime.now())
                        .getContent()
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "PAST":
                return bookingRepository.readAllBookerPastBookings(pageable, bookerId, LocalDateTime.now())
                        .getContent()
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "FUTURE":
                return bookingRepository.readAllBookerFutureBookings(pageable, bookerId, LocalDateTime.now())
                        .getContent()
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "WAITING":
                return bookingRepository
                        .findAllByBooker_IdAndStatusInOrderByStartDesc(pageable, bookerId,
                                List.of(BookingApproval.WAITING))
                        .getContent()
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "REJECTED":
                return bookingRepository
                        .findAllByBooker_IdAndStatusInOrderByStartDesc(pageable, bookerId,
                                List.of(BookingApproval.REJECTED))
                        .getContent()
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            default:
                return bookingRepository.findAllByBooker_IdOrderByStartDesc(pageable, bookerId)
                        .getContent()
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional
    public Collection<BookingDtoOutput> readAllOwnerItemBookings(long ownerId, String state, Pageable pageable) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + ownerId + " не найден"));

        List<Long> userItems = itemRepository.findAllByOwnerId(ownerId)
                .stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        switch (state) {
            case "CURRENT":
                return bookingRepository.readAllOwnerItemsCurrentBookings(pageable, userItems, LocalDateTime.now())
                        .getContent()
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "PAST":
                return bookingRepository.readAllOwnerItemsPastBookings(pageable, userItems, LocalDateTime.now())
                        .getContent()
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "FUTURE":
                return bookingRepository.readAllOwnerItemsFutureBookings(pageable, userItems, LocalDateTime.now())
                        .getContent()
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "WAITING":
                return bookingRepository
                        .findAllByItem_IdInAndStatusInOrderByStartDesc(pageable, userItems,
                                List.of(BookingApproval.WAITING))
                        .getContent()
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            case "REJECTED":
                return bookingRepository
                        .findAllByItem_IdInAndStatusInOrderByStartDesc(pageable, userItems,
                                List.of(BookingApproval.REJECTED))
                        .getContent()
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
            default:
                return bookingRepository.findAllByItem_IdInOrderByStartDesc(pageable, userItems)
                        .getContent()
                        .stream()
                        .map(BookingMapper::mapToBookingDtoOutput)
                        .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional
    public BookingDtoOutput updateApproval(long ownerId, long id, Boolean isApproved) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + ownerId + " не найден"));

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