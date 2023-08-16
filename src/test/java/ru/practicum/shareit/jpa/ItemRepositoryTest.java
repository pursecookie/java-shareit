package ru.practicum.shareit.jpa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class ItemRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    User owner;
    Item item;
    Pageable pageable;

    @BeforeEach
    void setUp() {
        owner = new User();

        owner.setName("User1");
        owner.setEmail("user1@mail.ru");

        userRepository.save(owner);

        item = new Item();

        item.setName("Item1");
        item.setDescription("Item1 Description");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setItemRequest(null);

        itemRepository.save(item);

        pageable = PageRequest.of(0, 10);
    }

    @AfterEach
    void deleteItems() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void search() {
        Page<Item> items = itemRepository.search("description", pageable);

        assertEquals(1, items.getContent().size());
        assertEquals(item.getName(), items.getContent().get(0).getName());
        assertEquals(item.getDescription(), items.getContent().get(0).getDescription());
    }
}