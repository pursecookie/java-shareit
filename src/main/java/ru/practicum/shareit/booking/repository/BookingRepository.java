package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingApproval;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBooker_IdOrderByStartDesc(long bookerId);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = ?1 " +
            "AND b.start <= ?2 " +
            "AND b.end >= ?2 " +
            "ORDER BY b.start DESC")
    List<Booking> readAllBookerCurrentBookings(long bookerId, LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = ?1 " +
            "AND b.start <= ?2 " +
            "AND b.end <= ?2 " +
            "ORDER BY b.start DESC")
    List<Booking> readAllBookerPastBookings(long bookerId, LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = ?1 " +
            "AND b.start >= ?2 " +
            "AND b.end >= ?2 " +
            "ORDER BY b.start DESC")
    List<Booking> readAllBookerFutureBookings(long bookerId, LocalDateTime now);

    List<Booking> findAllByBooker_IdAndStatusInOrderByStartDesc(long bookerId,
                                                                List<BookingApproval> status);

    List<Booking> findAllByItem_IdInOrderByStartDesc(List<Long> itemIds);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id IN ?1 " +
            "AND b.start <= ?2 " +
            "AND b.end >= ?2 " +
            "ORDER BY b.start DESC")
    List<Booking> readAllOwnerItemsCurrentBookings(List<Long> itemIds, LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id IN ?1 " +
            "AND b.start <= ?2 " +
            "AND b.end <= ?2 " +
            "ORDER BY b.start DESC")
    List<Booking> readAllOwnerItemsPastBookings(List<Long> itemIds, LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id IN ?1 " +
            "AND b.start >= ?2 " +
            "AND b.end >= ?2 " +
            "ORDER BY b.start DESC")
    List<Booking> readAllOwnerItemsFutureBookings(List<Long> itemIds, LocalDateTime now);

    List<Booking> findAllByItem_IdInAndStatusInOrderByStartDesc(List<Long> itemIds,
                                                                List<BookingApproval> status);

    List<Booking> findAllByItem_Id(long itemId);
}