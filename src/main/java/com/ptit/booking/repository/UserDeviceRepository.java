package com.ptit.booking.repository;

import com.ptit.booking.model.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    @Query("select ud from UserDevice ud where ud.user.id = :userId")
    List<UserDevice> findByUserId(@Param("userId") Long userId);
    boolean existsByDeviceToken(String deviceToken);

}
