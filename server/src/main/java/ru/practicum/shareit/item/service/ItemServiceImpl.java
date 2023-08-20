package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDtoForOwner;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingApproval;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ItemAvailabilityException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
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
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public ItemDtoWithRequestId create(long ownerId, ItemDtoInput itemDtoInput) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + ownerId + " не найден"));

        ItemRequest itemRequest = itemRequestRepository.findById(itemDtoInput.getRequestId())
                .orElse(null);

        Item item = ItemMapper.mapToItem(itemDtoInput, owner, itemRequest);

        return ItemMapper.mapToItemDtoWithRequestId(itemRepository.save(item), getRequestId(item));
    }

    @Override
    @Transactional
    public ItemDtoWithComments read(long userId, long id) {
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

        return ItemMapper.mapToItemDtoWithComments(item, lastBooking, nextBooking, comments);
    }

    @Override
    public Collection<ItemDtoWithComments> readAll(long ownerId, Pageable pageable) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + ownerId + " не найден"));

        return itemRepository.findAllByOwnerId(ownerId, pageable)
                .getContent()
                .stream()
                .map(item -> ItemMapper.mapToItemDtoWithComments(item,
                        BookingMapper.maptoBookingDtoForOwner(findLastBooking(item.getId())),
                        BookingMapper.maptoBookingDtoForOwner(findNextBooking(item.getId())),
                        findComments(item.getId())))
                .sorted(Comparator.comparingLong(ItemDtoWithComments::getId))
                .collect(Collectors.toList());
    }

    @Override
    public ItemDtoWithRequestId update(long ownerId, ItemDtoInput itemDtoInput, long id) {
        Item updatedItem = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + id + " не найдена"));

        if (updatedItem.getOwner().getId() != ownerId) {
            throw new AccessDeniedException("Нет прав для редактирования вещи");
        }

        if (itemDtoInput.getName() != null) {
            updatedItem.setName(itemDtoInput.getName());
        }

        if (itemDtoInput.getDescription() != null) {
            updatedItem.setDescription(itemDtoInput.getDescription());
        }

        if (itemDtoInput.getAvailable() != null) {
            updatedItem.setAvailable(itemDtoInput.getAvailable());
        }

        return ItemMapper.mapToItemDtoWithRequestId(itemRepository.save(updatedItem), getRequestId(updatedItem));
    }

    @Override
    public Collection<ItemDtoOutput> search(String text, Pageable pageable) {
        return itemRepository.search(text, pageable)
                .getContent()
                .stream()
                .filter(Item::getAvailable)
                .map(item -> ItemMapper.mapToItemDtoOutput(item, null, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto createComment(long authorId, CommentDto commentDto, long itemId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + authorId + " не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));

        List<Booking> itemBookings = bookingRepository.findAllByItem_Id(itemId)
                .stream()
                .filter(booking -> booking.getBooker().getId() == authorId)
                .filter(booking -> booking.getStatus() == BookingApproval.APPROVED)
                .collect(Collectors.toList());

        if (itemBookings.isEmpty()) {
            throw new ItemAvailabilityException("Пользователь с id " + authorId + " не бронировал вещь с id " + itemId);
        }

        List<Booking> pastOrPresentBookings = itemBookings.stream()
                .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());

        if (pastOrPresentBookings.isEmpty()) {
            throw new ItemAvailabilityException("Отзыв можно оставить только после состоявшегося бронирования");
        }

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

    private long getRequestId(Item item) {
        if (item.getItemRequest() == null) {
            return 0;
        } else {
            return item.getItemRequest().getId();
        }
    }

}