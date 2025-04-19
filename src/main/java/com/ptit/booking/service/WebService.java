package com.ptit.booking.service;

import org.springframework.http.ResponseEntity;

public interface WebService {
    ResponseEntity<?> getAllHotel();
    ResponseEntity<?> getBookingByHotel(Long hotelId);
    ResponseEntity<?> getBookingDetail(Long bookingId);
    ResponseEntity<?> checkInStatus(Long bookingId);
    ResponseEntity<?> checkOutStatus(Long bookingId);
    ResponseEntity<?> cancelStatus(Long bookingId);
}
