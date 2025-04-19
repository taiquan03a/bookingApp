package com.ptit.booking.repository;

import com.ptit.booking.model.Booking;
import com.ptit.booking.model.Hotel;
import com.ptit.booking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("select b " +
            "from Booking b " +
            "where b.user = :user " +
            "and b.status = :status")
    List<Booking> findAllByUserAndStatus(@Param("user") User user, @Param("status") String status);

    List<Booking> findByHotelAndStatus(Hotel hotel,String status);
}
