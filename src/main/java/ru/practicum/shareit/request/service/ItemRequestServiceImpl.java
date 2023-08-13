package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDtoInput;
import ru.practicum.shareit.request.dto.ItemRequestDtoOutput;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDtoOutput create(long requestorId, ItemRequestDtoInput itemRequestDtoInput) {
        User requestor = userRepository.findById(requestorId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + requestorId + " не найден"));

        ItemRequest itemRequest = ItemRequestMapper
                .mapToItemRequest(itemRequestDtoInput, requestor, LocalDateTime.now());

        return ItemRequestMapper.mapToItemRequestDtoOutput(itemRequestRepository.save(itemRequest),
                itemRepository.findAllByItemRequestId(itemRequest.getId())
                        .stream()
                        .map(item -> ItemMapper.mapToItemDtoWithRequestId(item, itemRequest.getId()))
                        .collect(Collectors.toList()));
    }

    @Override
    public Collection<ItemRequestDtoOutput> readAllRequestorRequests(long requestorId) {
        userRepository.findById(requestorId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + requestorId + " не найден"));

        return itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(requestorId).stream()
                .map(itemRequest -> ItemRequestMapper.mapToItemRequestDtoOutput(itemRequest,
                        itemRepository.findAllByItemRequestId(itemRequest.getId())
                                .stream()
                                .map(item -> ItemMapper.mapToItemDtoWithRequestId(item, itemRequest.getId()))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemRequestDtoOutput> readAllOtherUsersRequests(long userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        return itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId, pageable).stream()
                .map(itemRequest -> ItemRequestMapper.mapToItemRequestDtoOutput(itemRequest,
                        itemRepository.findAllByItemRequestId(itemRequest.getId())
                                .stream()
                                .map(item -> ItemMapper.mapToItemDtoWithRequestId(item, itemRequest.getId()))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDtoOutput read(long userId, long id) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        ItemRequest itemRequest = itemRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Запрос на вещь с id " + id + " не найден"));

        return ItemRequestMapper.mapToItemRequestDtoOutput(itemRequestRepository.findByIdOrderByCreatedDesc(id),
                itemRepository.findAllByItemRequestId(itemRequest.getId())
                        .stream()
                        .map(item -> ItemMapper.mapToItemDtoWithRequestId(item, itemRequest.getId()))
                        .collect(Collectors.toList()));
    }
}