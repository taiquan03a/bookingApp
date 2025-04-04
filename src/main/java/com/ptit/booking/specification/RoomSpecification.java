package com.ptit.booking.specification;

import com.ptit.booking.dto.hotelDetail.SelectRoomRequest;
import com.ptit.booking.model.Booking;
import com.ptit.booking.model.Hotel;
import com.ptit.booking.model.Room;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class RoomSpecification {
    public static Specification<Room> availableRooms(SelectRoomRequest selectRoomRequest, Hotel hotel) {

        return (root, query, criteriaBuilder) -> {
            // Subquery để lấy danh sách phòng đã đặt
            Subquery<Long> bookedRoomSubquery = query.subquery(Long.class);
            Root<Booking> bookingRoot = bookedRoomSubquery.from(Booking.class);

            bookedRoomSubquery.select(bookingRoot.get("room"))
                    .where(
                            criteriaBuilder.equal(bookingRoot.get("hotel"), hotel),
                            bookingRoot.get("status").in("BOOKED", "CHECKIN"),
                            criteriaBuilder.or(
                                    criteriaBuilder.between(
                                            bookingRoot.get("checkIn"),
                                            selectRoomRequest.getCheckInDate(),
                                            selectRoomRequest.getCheckOutDate()
                                    ),
                                    criteriaBuilder.between(
                                            bookingRoot.get("checkOut"),
                                            selectRoomRequest.getCheckInDate(),
                                            selectRoomRequest.getCheckOutDate()
                                    ),
                                    criteriaBuilder.and(
                                            criteriaBuilder.lessThanOrEqualTo(
                                                    bookingRoot.get("checkIn"),
                                                    selectRoomRequest.getCheckInDate()
                                            ),
                                            criteriaBuilder.greaterThanOrEqualTo(
                                                    bookingRoot.get("checkOut"),
                                                    selectRoomRequest.getCheckOutDate()
                                            )
                                    )
                            )
                    );

            // Truy vấn chính lấy phòng còn trống
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
