package ru.practicum.shareit.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    UserDto userDto1, userDto2, updatedUserDto;

    @BeforeEach
    void setUp() {
        userDto1 = new UserDto(1, "User1", "user1@mail.ru");
    }

    @Test
    @SneakyThrows
    void create_whenUserValid_thenStatus200AndReturnedUser() {
        Mockito.when(userService.create(Mockito.any())).thenReturn(userDto1);

        String result = mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userDto1)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Mockito.verify(userService).create(userDto1);

        assertEquals(objectMapper.writeValueAsString(userDto1), result);
    }

    @Test
    @SneakyThrows
    void create_whenNameBlank_thenStatus400() {
        userDto1.setName(null);

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userDto1)))
                .andExpect(status().isBadRequest());

        Mockito.verify(userService, Mockito.never()).create(userDto1);
    }

    @Test
    @SneakyThrows
    void create_whenEmailBlank_thenStatus400() {
        userDto1.setEmail(null);

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userDto1)))
                .andExpect(status().isBadRequest());

        Mockito.verify(userService, Mockito.never()).create(userDto1);
    }

    @Test
    @SneakyThrows
    void create_whenEmailInvalid_thenStatus400() {
        userDto1.setEmail("kukukikimail.ru");

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userDto1)))
                .andExpect(status().isBadRequest());

        Mockito.verify(userService, Mockito.never()).create(userDto1);
    }


    @Test
    @SneakyThrows
    void readTest() {
        Mockito.when(userService.read(1L)).thenReturn(userDto1);

        mockMvc.perform(get("/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("User1"))
                .andExpect(jsonPath("$.email").value("user1@mail.ru"));

        Mockito.verify(userService).read(1);
    }

    @Test
    @SneakyThrows
    void readAllTest() {
        userDto2 = new UserDto(2, "User2", "user2@mail.ru");

        Mockito.when(userService.readAll()).thenReturn(Arrays.asList(userDto1, userDto2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(Arrays.asList(userDto1, userDto2))));

        Mockito.verify(userService).readAll();
    }

    @Test
    @SneakyThrows
    void updateTest() {
        updatedUserDto = new UserDto(1, "Updated User1", "user1updated@mail.ru");

        Mockito.when(userService.update(Mockito.any(), Mockito.anyLong())).thenReturn(updatedUserDto);

        String result = mockMvc.perform(patch("/users/{id}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updatedUserDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Mockito.verify(userService).update(updatedUserDto, 1L);

        assertEquals(objectMapper.writeValueAsString(updatedUserDto), result);
    }

    @Test
    @SneakyThrows
    void deleteTest() {
        mockMvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isOk());

        Mockito.verify(userService).delete(1L);
    }

}