package com.ptit.booking.service.Impl;

import com.ptit.booking.constants.NotificationConstants;
import com.ptit.booking.enums.EnumNotificationType;
import com.ptit.booking.model.Booking;
import com.ptit.booking.model.Coupon;
import com.ptit.booking.model.User;
import com.ptit.booking.repository.BookingRepository;
import com.ptit.booking.repository.CouponRepository;
import com.ptit.booking.repository.UserRepository;
import com.ptit.booking.service.NotificationService;
import com.ptit.booking.service.Reminder;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@AllArgsConstructor
public class ReminderServiceImpl implements Reminder {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Scheduled(cron = "0 33 18 * * *")
    public void reminderCoupon() {
        System.out.println("___Check scheduled____");
        List<User> userList = userRepository.findAll();
        for (User user : userList) {
            List<Coupon> couponListByUser = couponRepository.findByUser(user);
            for (Coupon coupon : couponListByUser) {
                long time = ChronoUnit.HOURS.between(LocalDateTime.now(), coupon.getExpiryDate());
                if(time <= 24){
                    String title = NotificationConstants.Template.Reminder.TITLE_PROMO_EXPIRING;
                    String message = String.format(
                            NotificationConstants.Template.Reminder.MESSAGE_PROMO_EXPIRING,
                            coupon.getCode(),
                            coupon.getExpiryDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    );
                    notificationService.sendNotification(
                            user.getId(),
                            title,
                            message,
                            EnumNotificationType.REMINDER
                    );
                }
            }
        }
    }

    @Override
    @Scheduled(cron = "0 0 * * * *") // Chạy mỗi giờ
    public void reminderCheckin() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.plusHours(12);
        LocalDateTime to = from.plusHours(1); // kiểm tra khoảng 1 tiếng sau

        List<Booking> bookings = bookingRepository.findByCheckinTimeBetween(from, to);

        for (Booking booking : bookings) {
            String title = NotificationConstants.Template.Reminder.TITLE_CHECKIN_TOMORROW;
            String message = String.format(
                    NotificationConstants.Template.Reminder.MESSAGE_CHECKIN_TOMORROW,
                    booking.getHotel().getName(),
                    booking.getCheckIn().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );

            notificationService.sendNotification(
                    booking.getUser().getId(),
                    title,
                    message,
                    EnumNotificationType.REMINDER
            );
        }
    }

    @Override
    @Scheduled(cron = "0 0 * * * *")
    public void reminderCheckout() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.plusHours(12);
        LocalDateTime to = from.plusHours(1); // kiểm tra khoảng 1 tiếng sau

        List<Booking> bookings = bookingRepository.findByCheckoutTimeBetween(from, to);

        for (Booking booking : bookings) {
            String title = NotificationConstants.Template.Reminder.TITLE_CHECKOUT_TOMORROW;
            String message = String.format(
                    NotificationConstants.Template.Reminder.MESSAGE_CHECKOUT_TOMORROW,
                    booking.getHotel().getName(),
                    booking.getCheckIn().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );

            notificationService.sendNotification(
                    booking.getUser().getId(),
                    title,
                    message,
                    EnumNotificationType.REMINDER
            );
        }
    }
}
