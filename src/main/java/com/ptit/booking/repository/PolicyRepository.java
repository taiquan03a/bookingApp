package com.ptit.booking.repository;

import com.ptit.booking.model.Booking;
import com.ptit.booking.model.Hotel;
import com.ptit.booking.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    @Query("""
        select p
        from HotelPolicy hp
        left join Policy p on hp.policy = p
        where p.type = 'CANCEL' and hp.hotel = :hotel
    """)
    List<Policy> findCancelByHotel(@Param("hotel") Hotel hotel);
}
