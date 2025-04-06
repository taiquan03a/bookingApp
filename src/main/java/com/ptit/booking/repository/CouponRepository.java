package com.ptit.booking.repository;

import com.ptit.booking.model.Coupon;
import com.ptit.booking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    @Query("SELECT Coupon from UserCoupon uc where uc.user = :user")
    List<Coupon> findByUser(@Param("user") User user);
}
