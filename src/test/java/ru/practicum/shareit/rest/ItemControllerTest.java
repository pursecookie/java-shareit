package ru.practicum.shareit.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;
    User owner;
    Item item;
    Item updatedItem;
    ItemDtoInput itemDtoInput;
    ItemDtoInput updatedItemInput;
    ItemDtoWithRequestId itemDtoWithRequestId;
    ItemDtoWithComments itemDtoWithComments1;
    ItemDtoWithComments itemDtoWithComments2;
    ItemDtoOutput itemDtoOutput;
    Pageable pageable;
    CommentDto commentDto;

    @BeforeEach
    void setUp() {
        owner = new User(1, "User1", "user1@mail.ru");

        itemDtoInput = new ItemDtoInput(1, "Item1", "Item1 Description", true, 0);
        item = ItemMapper.mapToItem(itemDtoInput, owner, null);
        itemDtoWithRequestId = ItemMapper.mapToItemDtoWithRequestId(item, 0);
        itemDtoWithComments1 = ItemMapper
                .mapToItemDtoWithComments(item, null, null, new ArrayList<>());

        pageable = PageRequest.of(0, 10);
    }

    @Test
    @SneakyThrows
    void create_whenItemValid_thenStatus200AndReturnedUser() {
        Mockito.when(itemService.create(Mockito.anyLong(), Mockito.any())).thenReturn(itemDtoWithRequestId);

        String result = mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDtoInput)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Mockito.verify(itemService).create(Mockito.anyLong(), Mockito.any());

        assertEquals(objectMapper.writeValueAsString(itemDtoWithRequestId), result);
    }

    @Test
    @SneakyThrows
    void create_whenNameBlank_thenStatus400() {
        itemDtoInput.setName(null);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDtoInput)))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemService, Mockito.never()).create(1L, itemDtoInput);
    }

    @Test
    @SneakyThrows
    void create_whenDescriptionBlank_thenStatus400() {
        itemDtoInput.setDescription(null);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDtoInput)))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemService, Mockito.never()).create(1L, itemDtoInput);
    }

    @Test
    @SneakyThrows
    void create_whenAvailableNull_thenStatus400() {
        itemDtoInput.setAvailable(null);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDtoInput)))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemService, Mockito.never()).create(1L, itemDtoInput);
    }

    @Test
    @SneakyThrows
    void readTest() {
        Mockito.when(itemService.read(1L, 1L)).thenReturn(itemDtoWithComments1);

        mockMvc.perform(get("/items/{id}", 1L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Item1"))
                .andExpect(jsonPath("$.description").value("Item1 Description"))
                .andExpect(jsonPath("$.available").value("true"));

        Mockito.verify(itemService).read(1, 1);
    }

    @Test
    @SneakyThrows
    void readAllTest() {
        itemDtoWithComments2 = new ItemDtoWithComments(2, "Item2",
                "Item2 Description", true, null, null, new ArrayList<>());

        Mockito.when(itemService.readAll(1L, pageable))
                .thenReturn(Arrays.asList(itemDtoWithComments1, itemDtoWithComments2));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper
                        .writeValueAsString(Arrays.asList(itemDtoWithComments1, itemDtoWithComments2))));

        Mockito.verify(itemService).readAll(1L, pageable);
    }

    @Test
    @SneakyThrows
    void updateTest() {
        updatedItemInput = new ItemDtoInput(1, "Updated Item1",
                "Updated Description", true, 0);
        updatedItem = ItemMapper.mapToItem(updatedItemInput, owner, null);

        Mockito.when(itemService.update(Mockito.anyLong(), Mockito.any(), Mockito.anyLong()))
                .thenReturn(ItemMapper.mapToItemDtoWithRequestId(updatedItem, 0));

        String result = mockMvc.perform(patch("/items/{id}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updatedItemInput)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Mockito.verify(itemService).update(Mockito.anyLong(), Mockito.any(), Mockito.anyLong());

        assertEquals(objectMapper.writeValueAsString(ItemMapper
                .mapToItemDtoWithRequestId(updatedItem, 0)), result);
    }

    @Test
    @SneakyThrows
    void searchTest() {
        itemDtoOutput = ItemMapper.mapToItemDtoOutput(item, null, null);

        Mockito.when(itemService.search("description", pageable))
                .thenReturn(Collections.singletonList(itemDtoOutput));

        mockMvc.perform(get("/items/search")
                        .param("text", "description"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper
                        .writeValueAsString(Collections.singletonList(itemDtoOutput))));

        Mockito.verify(itemService).search("description", pageable);
    }

    @Test
    @SneakyThrows
    void createComment_whenCommentValid_thenStatus200AndReturnedComment() {
        commentDto = new CommentDto(1, "Comment to Item 1", owner.getName(), LocalDateTime.now());

        Mockito.when(itemService.createComment(Mockito.anyLong(), Mockito.any(), Mockito.anyLong()))
                .thenReturn(commentDto);

        String result = mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Mockito.verify(itemService).createComment(Mockito.anyLong(), Mockito.any(), Mockito.anyLong());

        assertEquals(objectMapper.writeValueAsString(commentDto), result);
    }

    @Test
    @SneakyThrows
    void createComment_whenTextBlank_thenStatus400() {
        commentDto = new CommentDto(1, "", owner.getName(), LocalDateTime.now());

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemService, Mockito.never())
                .createComment(Mockito.anyLong(), Mockito.any(), Mockito.anyLong());
    }

}