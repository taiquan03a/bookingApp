package com.ptit.booking.repository;

import com.ptit.booking.model.ServiceRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRoomRepository extends JpaRepository<ServiceRoom, Long> {
}
