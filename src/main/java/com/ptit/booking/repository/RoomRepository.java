package com.ptit.booking.repository;

import com.ptit.booking.model.Hotel;
import com.ptit.booking.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> , JpaSpecificationExecutor<Room> {
    @Query("SELECT MIN(r.price) FROM Room r WHERE r.hotel = :hotel")
    float getRoomPriceMin(@Param("hotel") Hotel hotel);

    @Query(value = """
        WITH booked_rooms AS (
            SELECT room_id, SUM(room_count) AS booked_count
            FROM bookings
            WHERE hotel_id = :hotelId
              AND status IN ('BOOKED', 'CHECKIN')
              AND (
                (check_in BETWEEN :checkin AND :checkout) OR
                (check_out BETWEEN :checkin AND :checkout) OR
                (:checkin BETWEEN check_in AND check_out)
              )
            GROUP BY room_id
        )
        SELECT r.*
        FROM rooms r
        LEFT JOIN booked_rooms br ON r.id = br.room_id
        WHERE r.hotel_id = :hotelId
          AND (r.room_count - COALESCE(br.booked_count, 0)) >= :roomCount
          AND r.max_adults >= :maxAdults
          AND r.max_children >= :maxChildren
          AND r.availability = 1
        """, nativeQuery = true)
    List<Room> findAvailableRooms(
            @Param("hotelId") Long hotelId,
            @Param("checkin") LocalDate checkin,
            @Param("checkout") LocalDate checkout,
            @Param("roomCount") int roomCount,
            @Param("maxAdults") int maxAdults,
            @Param("maxChildren") int maxChildren
    );

    @Query("SELECT (r.roomCount - COALESCE(SUM(b.roomCount), 0)) " +
            "FROM Room r " +
            "LEFT JOIN BookingRoom br ON r = br.room " +
            "LEFT JOIN Booking b ON br.booking = b " +
            "AND b.status IN ('BOOKED','CHECKIN') " +
            "AND (" +
            "   :checkin BETWEEN b.checkIn AND b.checkOut " +
            "   OR :checkout BETWEEN b.checkIn AND b.checkOut " +
            "   OR (b.checkIn <= :checkin AND b.checkOut >= :checkout) " +
            ") " +
            "WHERE r = :room " +
            "GROUP BY r.roomCount")
    int countAvailableRoom(@Param("room") Room room,
                           @Param("checkin") LocalDate checkin,
                           @Param("checkout") LocalDate checkout);
}
