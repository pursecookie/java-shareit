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
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingApproval;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;
    Booking booking;
    BookingDtoOutput bookingDtoOutput;
    BookingDtoInput bookingDtoInput;
    Item item;
    User owner;
    User booker;
    Pageable pageable;

    @BeforeEach
    void setUp() {
        owner = new User(1, "User1", "user1@mail.ru");
        item = new Item(1, "Item1", "Item1 Description", true, owner, null);
        booker = new User(2, "User2", "user2@mail.ru");

        bookingDtoInput = new BookingDtoInput(1, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2), 1);
        booking = BookingMapper.mapToBooking(bookingDtoInput, item, booker, BookingApproval.WAITING);
        bookingDtoOutput = BookingMapper.mapToBookingDtoOutput(booking);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    @SneakyThrows
    void create_whenBookingValid_thenStatus200AndReturnedBooking() {
        Mockito.when(bookingService.create(Mockito.anyLong(), Mockito.any())).thenReturn(bookingDtoOutput);

        String result = mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingDtoInput)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Mockito.verify(bookingService).create(Mockito.anyLong(), Mockito.any());

        assertEquals(objectMapper.writeValueAsString(bookingDtoOutput), result);
    }

    @Test
    @SneakyThrows
    void readTest() {
        Mockito.when(bookingService.read(2L, 1L)).thenReturn(bookingDtoOutput);

        mockMvc.perform(get("/bookings/{id}", 1L)
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));

        Mockito.verify(bookingService).read(2L, 1L);
    }

    @Test
    @SneakyThrows
    void readAllBookerBookings_whenStatusSupported_thenReturnedBookings() {
        Mockito.when(bookingService.readAllBookerBookings(2L, "ALL", pageable))
                .thenReturn(Collections.singletonList(bookingDtoOutput));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper
                        .writeValueAsString(Collections.singletonList(bookingDtoOutput))));

        Mockito.verify(bookingService).readAllBookerBookings(2L, "ALL", pageable);
    }

    @Test
    @SneakyThrows
    void readAllOwnerItemBookingsTest() {
        Mockito.when(bookingService.readAllOwnerItemBookings(1L, "ALL", pageable))
                .thenReturn(Collections.singletonList(bookingDtoOutput));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper
                        .writeValueAsString(Collections.singletonList(bookingDtoOutput))));

        Mockito.verify(bookingService).readAllOwnerItemBookings(1L, "ALL", pageable);
    }

    @Test
    @SneakyThrows
    void updateApprovalTest() {
        bookingDtoOutput.setStatus(BookingApproval.APPROVED);

        Mockito.when(bookingService.updateApproval(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyBoolean()))
                .thenReturn(bookingDtoOutput);

        String result = mockMvc.perform(patch("/bookings/{id}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Mockito.verify(bookingService).updateApproval(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyBoolean());

        assertEquals(objectMapper.writeValueAsString(bookingDtoOutput), result);
    }

}