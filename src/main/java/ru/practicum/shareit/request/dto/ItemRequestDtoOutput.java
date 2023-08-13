package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.item.dto.ItemDtoWithRequestId;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ItemRequestDtoOutput {
    private long id;
    private String description;
    private LocalDateTime created;
    private List<ItemDtoWithRequestId> items;
}