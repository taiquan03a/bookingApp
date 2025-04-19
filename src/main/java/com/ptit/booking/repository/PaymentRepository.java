package com.ptit.booking.repository;

import com.ptit.booking.model.Booking;
import com.ptit.booking.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBooking(Booking booking);
    Payment findByAppTransId(String appTransId);
}
