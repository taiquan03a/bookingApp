package com.ptit.booking.repository;

import com.ptit.booking.model.Coupon;
import com.ptit.booking.model.User;
import com.ptit.booking.model.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    UserCoupon findByUserAndCoupon(User user, Coupon coupon);
}
