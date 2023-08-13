package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingApproval;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findAllByBooker_IdOrderByStartDesc(Pageable pageable, long bookerId);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = ?1 " +
            "AND b.start <= ?2 " +
            "AND b.end >= ?2 " +
            "ORDER BY b.start DESC")
    Page<Booking> readAllBookerCurrentBookings(Pageable pageable, long bookerId, LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = ?1 " +
            "AND b.start <= ?2 " +
            "AND b.end <= ?2 " +
            "ORDER BY b.start DESC")
    Page<Booking> readAllBookerPastBookings(Pageable pageable, long bookerId, LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = ?1 " +
            "AND b.start >= ?2 " +
            "AND b.end >= ?2 " +
            "ORDER BY b.start DESC")
    Page<Booking> readAllBookerFutureBookings(Pageable pageable, long bookerId, LocalDateTime now);

    Page<Booking> findAllByBooker_IdAndStatusInOrderByStartDesc(Pageable pageable, long bookerId,
                                                                List<BookingApproval> status);

    Page<Booking> findAllByItem_IdInOrderByStartDesc(Pageable pageable, List<Long> itemIds);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id IN ?1 " +
            "AND b.start <= ?2 " +
            "AND b.end >= ?2 " +
            "ORDER BY b.start DESC")
    Page<Booking> readAllOwnerItemsCurrentBookings(Pageable pageable, List<Long> itemIds, LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id IN ?1 " +
            "AND b.start <= ?2 " +
            "AND b.end <= ?2 " +
            "ORDER BY b.start DESC")
    Page<Booking> readAllOwnerItemsPastBookings(Pageable pageable, List<Long> itemIds, LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id IN ?1 " +
            "AND b.start >= ?2 " +
            "AND b.end >= ?2 " +
            "ORDER BY b.start DESC")
    Page<Booking> readAllOwnerItemsFutureBookings(Pageable pageable, List<Long> itemIds, LocalDateTime now);

    Page<Booking> findAllByItem_IdInAndStatusInOrderByStartDesc(Pageable pageable, List<Long> itemIds,
                                                                List<BookingApproval> status);

    List<Booking> findAllByItem_Id(long itemId);
}