package com.ptit.booking.repository;

import com.ptit.booking.model.BookingRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRoomRepository extends JpaRepository<BookingRoom, Long> {

}
