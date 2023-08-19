package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
public class ItemRequestDtoInput {
    private long id;

    @NotBlank(message = "Описание запроса не может быть пустым")
    private String description;
}