package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingApproval;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBooker_IdOrderByStartDesc(long bookerId);

    List<Booking> findAllByBooker_IdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(long bookerId,
                                                                                  LocalDateTime now1,
                                                                                  LocalDateTime now2);

    List<Booking> findAllByBooker_IdAndStartIsBeforeAndEndIsBeforeOrderByStartDesc(long bookerId,
                                                                                   LocalDateTime now1,
                                                                                   LocalDateTime now2);

    List<Booking> findAllByBooker_IdAndStartIsAfterAndEndIsAfterOrderByStartDesc(long bookerId,
                                                                                 LocalDateTime now1,
                                                                                 LocalDateTime now2);

    List<Booking> findAllByBooker_IdAndStatusInOrderByStartDesc(long bookerId,
                                                                List<BookingApproval> status);

    List<Booking> findAllByItem_IdInOrderByStartDesc(List<Long> itemIds);

    List<Booking> findAllByItem_IdInAndStartIsBeforeAndEndIsAfterOrderByStartDesc(List<Long> itemIds,
                                                                                  LocalDateTime now1,
                                                                                  LocalDateTime now2);

    List<Booking> findAllByItem_IdInAndStartIsBeforeAndEndIsBeforeOrderByStartDesc(List<Long> itemIds,
                                                                                   LocalDateTime now1,
                                                                                   LocalDateTime now2);

    List<Booking> findAllByItem_IdInAndStartIsAfterAndEndIsAfterOrderByStartDesc(List<Long> itemIds,
                                                                                 LocalDateTime now1,
                                                                                 LocalDateTime now2);

    List<Booking> findAllByItem_IdInAndStatusInOrderByStartDesc(List<Long> itemIds,
                                                                List<BookingApproval> status);

    List<Booking> findAllByItem_Id(long itemId);

}