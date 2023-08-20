package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.UnsupportedStatusException;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") long bookerId,
                                         @Valid @RequestBody BookingDtoInput bookingDtoInput) {
        return bookingClient.create(bookerId, bookingDtoInput);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> read(@RequestHeader("X-Sharer-User-Id") long userId,
                                       @PathVariable long id) {
        return bookingClient.read(userId, id);
    }

    @GetMapping
    public ResponseEntity<Object> readAllBookerBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                                        @RequestParam(defaultValue = "ALL") String state,
                                                        @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                                        @RequestParam(defaultValue = "10")
                                                        @Min(1) @Max(200) Integer size) {
        BookingState.from(state)
                .orElseThrow(() -> new UnsupportedStatusException("Unknown state: " + state));

        return bookingClient.readAllBookerBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> readAllOwnerItemBookings(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                                           @RequestParam(defaultValue = "ALL") String state,
                                                           @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                                           @RequestParam(defaultValue = "10")
                                                           @Min(1) @Max(200) Integer size) {
        BookingState.from(state)
                .orElseThrow(() -> new UnsupportedStatusException("Unknown state: " + state));

        return bookingClient.readAllOwnerItemBookings(ownerId, state, from, size);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateApproval(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                                 @PathVariable long id,
                                                 @RequestParam("approved") Boolean isApproved) {
        return bookingClient.updateApproval(ownerId, id, isApproved);
    }
}