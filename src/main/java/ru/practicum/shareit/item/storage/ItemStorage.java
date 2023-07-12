package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class ItemStorage {
    private final Map<Long, Item> itemStorage = new HashMap<>();
    private int counter = 1;

    public Item create(Item item) {
        item.setId(counter++);
        itemStorage.put(item.getId(), item);

        return itemStorage.get(item.getId());
    }

    public Item read(long id) {
        return itemStorage.get(id);
    }

    public Collection<Item> readAll(long ownerId) {
        return itemStorage.values().stream()
                .filter(item -> item.getOwner().getId() == ownerId)
                .collect(Collectors.toList());
    }

    public Item update(Item item) {
        Item updatedItem = itemStorage.get(item.getId());

        if (item.getName() != null) {
            updatedItem.setName(item.getName());
        }

        if (item.getDescription() != null) {
            updatedItem.setDescription(item.getDescription());
        }

        if (item.getAvailable() != null) {
            updatedItem.setAvailable(item.getAvailable());
        }

        return updatedItem;
    }

    public Collection<Item> search(String text) {
        if (text.isEmpty()) {
            return new ArrayList<>();
        }

        return itemStorage.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toUpperCase().contains(text.toUpperCase()) ||
                        item.getDescription().toUpperCase().contains(text.toUpperCase()))
                .collect(Collectors.toList());
    }

    public boolean isNotExists(long id) {
        return !itemStorage.containsKey(id);
    }
}