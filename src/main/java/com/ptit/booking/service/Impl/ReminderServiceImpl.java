package com.ptit.booking.service.Impl;

import com.ptit.booking.model.Coupon;
import com.ptit.booking.model.User;
import com.ptit.booking.repository.CouponRepository;
import com.ptit.booking.repository.UserRepository;
import com.ptit.booking.service.Reminder;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@AllArgsConstructor
public class ReminderServiceImpl implements Reminder {


    private final UserRepository userRepository;
    private final CouponRepository couponRepository;

    @Override
    @Scheduled(cron = "0 0 9 * * *")
    public void reminderCoupon() {
        List<User> userList = userRepository.findAll();
        for (User user : userList) {
            List<Coupon> couponListByUser = couponRepository.findByUser(user);
            for (Coupon coupon : couponListByUser) {
                long time = ChronoUnit.HOURS.between(LocalDateTime.now(), coupon.getExpiryDate());
            }
        }
    }

    @Override
    public void reminderCheckin() {

    }

    @Override
    public void reminderCheckout() {

    }
}
