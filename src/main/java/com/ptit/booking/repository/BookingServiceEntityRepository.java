package com.ptit.booking.repository;

import com.ptit.booking.model.BookingServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingServiceEntityRepository extends JpaRepository<BookingServiceEntity, Long> {
}
