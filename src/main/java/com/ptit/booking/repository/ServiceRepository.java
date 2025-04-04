package com.ptit.booking.repository;

import com.ptit.booking.model.Room;
import com.ptit.booking.model.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Set;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
    @Query("SELECT s FROM ServiceRoom sr " +
            "JOIN sr.serviceEntity s " +
            "WHERE sr.room = :room AND s.serviceType = 'AMENITY'")
    Set<ServiceEntity> findAllByRoom(@Param("room") Room room);
}
