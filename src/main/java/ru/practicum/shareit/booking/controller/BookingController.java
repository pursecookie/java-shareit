package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.UnsupportedStatusException;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Collection;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDtoOutput create(@RequestHeader("X-Sharer-User-Id") long bookerId,
                                   @Valid @RequestBody BookingDtoInput bookingDtoInput) {
        return bookingService.create(bookerId, bookingDtoInput);
    }

    @GetMapping("/{id}")
    public BookingDtoOutput read(@RequestHeader("X-Sharer-User-Id") long userId,
                                 @PathVariable long id) {
        return bookingService.read(userId, id);
    }

    @GetMapping
    public Collection<BookingDtoOutput> readAllBookerBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                                              @RequestParam(defaultValue = "ALL") String state,
                                                              @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                                              @RequestParam(defaultValue = "10")
                                                              @Min(1) @Max(200) Integer size) {
        BookingState.from(state)
                .orElseThrow(() -> new UnsupportedStatusException("Unknown state: " + state));

        Pageable pageable = PageRequest.of(from / size, size);

        return bookingService.readAllBookerBookings(userId, state, pageable);
    }

    @GetMapping("/owner")
    public Collection<BookingDtoOutput> readAllOwnerItemBookings(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                                                 @RequestParam(defaultValue = "ALL") String state,
                                                                 @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                                                 @RequestParam(defaultValue = "10")
                                                                 @Min(1) @Max(200) Integer size) {
        BookingState.from(state)
                .orElseThrow(() -> new UnsupportedStatusException("Unknown state: " + state));

        Pageable pageable = PageRequest.of(from / size, size);

        return bookingService.readAllOwnerItemBookings(ownerId, state, pageable);
    }

    @PatchMapping("/{id}")
    public BookingDtoOutput updateApproval(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                           @PathVariable long id,
                                           @RequestParam("approved") Boolean isApproved) {
        return bookingService.updateApproval(ownerId, id, isApproved);
    }
}