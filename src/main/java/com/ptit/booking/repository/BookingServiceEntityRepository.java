package com.ptit.booking.repository;

import com.ptit.booking.model.Booking;
import com.ptit.booking.model.BookingRoom;
import com.ptit.booking.model.BookingServiceEntity;
import com.ptit.booking.model.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingServiceEntityRepository extends JpaRepository<BookingServiceEntity, Long> {

    @Query("""
        select bs from BookingServiceEntity bs where bs.service = :serviceEntity and bs.booking = :booking
    """)
    BookingServiceEntity findByBookingAndServiceEntity(
            @Param("booking") BookingRoom booking,
            @Param("serviceEntity") ServiceEntity serviceEntity);
}
