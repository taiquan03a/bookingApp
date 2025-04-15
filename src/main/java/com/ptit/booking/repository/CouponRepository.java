package com.ptit.booking.repository;

import com.ptit.booking.model.Coupon;
import com.ptit.booking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    @Query("SELECT Coupon from UserCoupon uc where uc.user = :user")
    List<Coupon> findByUser(@Param("user") User user);

    @Query("""
        SELECT c FROM Coupon c
        JOIN UserCoupon uc ON c = uc.coupon
        WHERE uc.user = :user
          AND uc.use = false
          AND c.status = true
          AND c.currentUsage < c.maxUsage
          AND c.validFromDate <= CURRENT_TIMESTAMP
          AND c.expiryDate >= CURRENT_TIMESTAMP
          AND c.minBookingAmount <= :totalPrice
          AND c.code LIKE %:code% 
        ORDER BY 
          CASE 
            WHEN c.discountType = 'PERCENTAGE' THEN (:totalPrice * c.discountValue / 100)
            ELSE c.discountValue
          END DESC
    """)
    List<Coupon> findBestCouponByUser(
            @Param("code") String code,
            @Param("user") User user,
            @Param("totalPrice") BigDecimal totalPrice
    );
    @Query("""
        SELECT  
          CASE 
            WHEN c.discountType = 'PERCENTAGE' THEN (:totalPrice * c.discountValue / 100)
            ELSE c.discountValue
          END
        FROM Coupon c
        WHERE c = :coupon
    """)
    BigDecimal calculateDiscountAmount(
            @Param("coupon") Coupon coupon,
            @Param("totalPrice") BigDecimal totalPrice
    );
    @Query("select c from Coupon c where c.code = :code")
    Coupon findByCouponCode(@Param("code") String code);



}
