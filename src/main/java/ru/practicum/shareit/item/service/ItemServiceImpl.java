package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDtoForOwner;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingApproval;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ItemAvailabilityException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto create(long ownerId, ItemDto itemDto) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + ownerId + " не найден"));

        Item item = ItemMapper.mapToItem(itemDto, owner);

        return ItemMapper.mapToItemDto(itemRepository.save(item), null, null, null);
    }

    @Override
    public ItemDto read(long userId, long id) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + id + " не найдена"));

        BookingDtoForOwner lastBooking = null;
        BookingDtoForOwner nextBooking = null;
        long ownerId = item.getOwner().getId();

        if (ownerId == userId) {
            lastBooking = BookingMapper.maptoBookingDtoForOwner(findLastBooking(id));
            nextBooking = BookingMapper.maptoBookingDtoForOwner(findNextBooking(id));
        }

        List<CommentDto> comments = findComments(id);

        return ItemMapper.mapToItemDto(item, lastBooking, nextBooking, comments);
    }

    @Override
    public Collection<ItemDto> readAll(long ownerId) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + ownerId + " не найден"));

        return itemRepository.findAllByOwnerId(ownerId).stream()
                .map(item -> ItemMapper.mapToItemDto(item,
                        BookingMapper.maptoBookingDtoForOwner(findLastBooking(item.getId())),
                        BookingMapper.maptoBookingDtoForOwner(findNextBooking(item.getId())),
                        findComments(item.getId())))
                .sorted(Comparator.comparingLong(ItemDto::getId))
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto update(long ownerId, ItemDto itemDto, long id) {
        Item updatedItem = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + id + " не найдена"));

        if (updatedItem.getOwner().getId() != ownerId) {
            throw new AccessDeniedException("Нет прав для редактирования вещи");
        }

        if (itemDto.getName() != null) {
            updatedItem.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            updatedItem.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            updatedItem.setAvailable(itemDto.getAvailable());
        }

        return ItemMapper.mapToItemDto(itemRepository.save(updatedItem),
                BookingMapper.maptoBookingDtoForOwner(findLastBooking(id)),
                BookingMapper.maptoBookingDtoForOwner(findNextBooking(id)),
                findComments(id));
    }

    @Override
    public Collection<ItemDto> search(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }

        return itemRepository.search(text).stream()
                .filter(Item::getAvailable)
                .map(item -> ItemMapper.mapToItemDto(item, null, null,
                        findComments(item.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto createComment(long authorId, CommentDto commentDto, long itemId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + authorId + " не найден"));

        List<Booking> itemBookings = bookingRepository.findAllByItem_Id(itemId)
                .stream()
                .filter(booking -> booking.getBooker().getId() == authorId)
                .filter(booking -> booking.getStatus() == BookingApproval.APPROVED)
                .collect(Collectors.toList());

        if (itemBookings.size() == 0) {
            throw new ItemAvailabilityException("Пользователь с id " + authorId + " не бронировал вещь с id " + itemId);
        }

        List<Booking> pastOrPresentBookings = itemBookings.stream()
                .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());

        if (pastOrPresentBookings.size() == 0) {
            throw new ItemAvailabilityException("Отзыв можно оставить только после состоявшегося бронирование");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));

        commentDto.setCreated(LocalDateTime.now());

        Comment comment = CommentMapper.mapToComment(commentDto, item, author);

        return CommentMapper.mapToCommentDto(commentRepository.save(comment));
    }

    private Booking findLastBooking(long itemId) {
        List<Booking> itemBookings = bookingRepository.findAllByItem_Id(itemId);

        return itemBookings.stream()
                .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()))
                .filter(booking -> booking.getStatus() == BookingApproval.APPROVED)
                .max(Comparator.comparing(Booking::getEnd))
                .orElse(null);
    }

    private Booking findNextBooking(long itemId) {
        List<Booking> itemBookings = bookingRepository.findAllByItem_Id(itemId);

        return itemBookings.stream()
                .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                .filter(booking -> booking.getStatus() == BookingApproval.APPROVED)
                .min(Comparator.comparing(Booking::getStart))
                .orElse(null);
    }

    private List<CommentDto> findComments(long itemId) {
        return commentRepository.findAllByItemId(itemId)
                .stream()
                .map(CommentMapper::mapToCommentDto)
                .collect(Collectors.toList());
    }
}