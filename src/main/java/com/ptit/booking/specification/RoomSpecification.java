package com.ptit.booking.specification;

import com.ptit.booking.dto.hotelDetail.SelectRoomRequest;
import com.ptit.booking.model.Booking;
import com.ptit.booking.model.BookingRoom;
import com.ptit.booking.model.Hotel;
import com.ptit.booking.model.Room;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class RoomSpecification {
    public static Specification<Room> availableRooms(SelectRoomRequest selectRoomRequest, Hotel hotel) {
        return (root, query, criteriaBuilder) -> {
            // Subquery lấy room_id đã được đặt trong khoảng thời gian giao nhau
            Subquery<Long> bookedRoomSubquery = query.subquery(Long.class);
            Root<BookingRoom> bookingRoomRoot = bookedRoomSubquery.from(BookingRoom.class);
            Join<BookingRoom, Booking> bookingJoin = bookingRoomRoot.join("booking"); // join sang booking để lấy hotel

            bookedRoomSubquery.select(bookingRoomRoot.get("room").get("id"))
                    .where(
                            criteriaBuilder.equal(bookingJoin.get("hotel"), hotel), // lọc theo hotel
                            bookingJoin.get("status").in("BOOKED", "CHECKIN"),
                            criteriaBuilder.or(
                                    criteriaBuilder.between(
                                            bookingJoin.get("checkIn"),
                                            selectRoomRequest.getCheckInDate(),
                                            selectRoomRequest.getCheckOutDate()
                                    ),
                                    criteriaBuilder.between(
                                            bookingJoin.get("checkOut"),
                                            selectRoomRequest.getCheckInDate(),
                                            selectRoomRequest.getCheckOutDate()
                                    ),
                                    criteriaBuilder.and(
                                            criteriaBuilder.lessThanOrEqualTo(
                                                    bookingJoin.get("checkIn"),
                                                    selectRoomRequest.getCheckInDate()
                                            ),
                                            criteriaBuilder.greaterThanOrEqualTo(
                                                    bookingJoin.get("checkOut"),
                                                    selectRoomRequest.getCheckOutDate()
                                            )
                                    )
                            )
                    );

            // Truy vấn chính: lấy các phòng còn trống
            return criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("hotel"), hotel),
                    root.get("availability").in(true),
                    criteriaBuilder.not(root.get("id").in(bookedRoomSubquery)),
                    criteriaBuilder.greaterThanOrEqualTo(root.get("roomCount"), selectRoomRequest.getRoomNumber()),
                    criteriaBuilder.greaterThanOrEqualTo(root.get("maxAdults"), selectRoomRequest.getAdults()),
                    criteriaBuilder.greaterThanOrEqualTo(root.get("maxChildren"), selectRoomRequest.getChildren())
            );
        };
    }

}
