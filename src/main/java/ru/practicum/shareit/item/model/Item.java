package ru.practicum.shareit.item.model;

import lombok.Data;

@Data
public class Item {
    private long id;
    private String name;
    private String description;
    private Boolean available;
    private long ownerId;

    public Item(long id, String name, String description, Boolean available) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
    }
}