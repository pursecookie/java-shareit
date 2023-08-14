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
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDtoInput;
import ru.practicum.shareit.request.dto.ItemRequestDtoOutput;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService itemRequestService;
    ItemRequestDtoInput itemRequestDtoInput;
    ItemRequest itemRequest;
    ItemRequestDtoOutput itemRequestDtoOutput;
    User requestor;
    User user;
    Pageable pageable;

    @BeforeEach
    void setUp() {
        requestor = new User(1, "User1", "user1@mail.ru");
        user = new User(2, "User2", "user2@mail.ru");

        itemRequestDtoInput = new ItemRequestDtoInput(1, "Item Request");
        itemRequest = ItemRequestMapper.mapToItemRequest(itemRequestDtoInput,
                requestor, LocalDateTime.now().plusMinutes(30));
        itemRequestDtoOutput = ItemRequestMapper.mapToItemRequestDtoOutput(itemRequest, new ArrayList<>());

        pageable = PageRequest.of(0, 10);
    }

    @Test
    @SneakyThrows
    void create_whenRequestValid_thenStatus200AndReturnedRequest() {
        Mockito.when(itemRequestService.create(Mockito.anyLong(), Mockito.any()))
                .thenReturn(itemRequestDtoOutput);

        String result = mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemRequestDtoInput)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Mockito.verify(itemRequestService).create(Mockito.anyLong(), Mockito.any());

        assertEquals(objectMapper.writeValueAsString(itemRequestDtoOutput), result);
    }

    @Test
    @SneakyThrows
    void create_whenDescriptionBlank_thenStatus400() {
        itemRequestDtoInput.setDescription(null);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemRequestDtoInput)))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemRequestService, Mockito.never()).create(1L, itemRequestDtoInput);
    }

    @Test
    @SneakyThrows
    void readAllRequestorRequestsTest() {
        Mockito.when(itemRequestService.readAllRequestorRequests(1L))
                .thenReturn(Collections.singletonList(itemRequestDtoOutput));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper
                        .writeValueAsString(Collections.singletonList(itemRequestDtoOutput))));

        Mockito.verify(itemRequestService).readAllRequestorRequests(1L);
    }

    @Test
    @SneakyThrows
    void readAllOtherUsersRequestsTest() {
        Mockito.when(itemRequestService.readAllOtherUsersRequests(2L, pageable))
                .thenReturn(Collections.singletonList(itemRequestDtoOutput));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper
                        .writeValueAsString(Collections.singletonList(itemRequestDtoOutput))));

        Mockito.verify(itemRequestService).readAllOtherUsersRequests(2L, pageable);
    }

    @Test
    @SneakyThrows
    void readTest() {
        Mockito.when(itemRequestService.read(1L, 1L)).thenReturn(itemRequestDtoOutput);

        mockMvc.perform(get("/requests/{id}", 1L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.description").value("Item Request"));

        Mockito.verify(itemRequestService).read(1, 1);
    }
}