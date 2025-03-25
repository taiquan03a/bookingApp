package com.ptit.booking.specification;

import com.ptit.booking.dto.hotel.FilterRequest;
import com.ptit.booking.model.Booking;
import com.ptit.booking.model.Hotel;
import com.ptit.booking.model.Room;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import jakarta.persistence.criteria.Predicate;
import java.util.*;

public class HotelSpecification {
    public static Specification<Hotel> filterHotels(FilterRequest filterRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filterRequest.getLocationId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("location").get("id"), filterRequest.getLocationId()));
            }

            if (filterRequest.getAdults() > 0) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("maxAdults"), filterRequest.getAdults()));
            }

            if (filterRequest.getChildren() > 0) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("maxChildren"), filterRequest.getChildren()));
            }

            if (filterRequest.getRoomNumber() > 0) {
                // Subquery để tính tổng số phòng của khách sạn
                Subquery<Long> totalRoomsSubquery = query.subquery(Long.class);
                Root<Room> roomRoot = totalRoomsSubquery.from(Room.class);
                totalRoomsSubquery.select(criteriaBuilder.coalesce(criteriaBuilder.sum(roomRoot.get("roomCount")), 0L));
                totalRoomsSubquery.where(criteriaBuilder.equal(roomRoot.get("hotel"), root));

                // Subquery để tính tổng số phòng đã được booked
                Subquery<Long> bookedRoomsSubquery = query.subquery(Long.class);
                Root<Booking> bookingRoot = bookedRoomsSubquery.from(Booking.class);
                bookedRoomsSubquery.select(criteriaBuilder.coalesce(criteriaBuilder.sum(bookingRoot.get("roomCount")), 0L));
                bookedRoomsSubquery.where(
                        criteriaBuilder.equal(bookingRoot.get("hotel"), root),
                        criteriaBuilder.equal(bookingRoot.get("status"),"BOOKED"),
                        criteriaBuilder.lessThanOrEqualTo(bookingRoot.get("checkIn"), filterRequest.getCheckout()),
                        criteriaBuilder.greaterThanOrEqualTo(bookingRoot.get("checkOut"), filterRequest.getCheckin())
                );

                // Tính số phòng trống = Tổng số phòng - số phòng đã booked
                Expression<Long> availableRooms = criteriaBuilder.diff(totalRoomsSubquery, bookedRoomsSubquery);

                // Điều kiện chính: availableRooms >= roomNumber yêu cầu
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        availableRooms,
                        criteriaBuilder.literal((long) filterRequest.getRoomNumber())
                ));
            }


            if (!filterRequest.getAmenityIds().isEmpty()) {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<Hotel> subRoot = subquery.from(Hotel.class);

                subquery.select(subRoot.get("id"))
                        .where(
                                criteriaBuilder.equal(subRoot, root),
                                subRoot.join("amenities").get("id").in(filterRequest.getAmenityIds())
                        )
                        .groupBy(subRoot.get("id"))
                        .having(criteriaBuilder.equal(
                                criteriaBuilder.count(subRoot.get("id")),
                                filterRequest.getAmenityIds().size()
                        ));

                predicates.add(criteriaBuilder.exists(subquery));
            }


//            if (!filterRequest.getServiceIds().isEmpty()) {
//                for (Long serviceId : filterRequest.getServiceIds()) {
//                    predicates.add(criteriaBuilder.isTrue(root.join("services").get("id").in(serviceId)));
//                }
//            }


            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

