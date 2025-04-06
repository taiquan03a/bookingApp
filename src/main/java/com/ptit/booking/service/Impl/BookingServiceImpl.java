package com.ptit.booking.service.Impl;

import com.ptit.booking.dto.room.BookingRoomRequest;
import com.ptit.booking.repository.CouponRepository;
import com.ptit.booking.repository.RoomRepository;
import com.ptit.booking.repository.ServiceRepository;
import com.ptit.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final RoomRepository roomRepository;
    private final CouponRepository couponRepository;
    private final ServiceRepository serviceRepository;

    @Override
    public ResponseEntity<?> booking(BookingRoomRequest bookingRoomRequest, Principal principal) {
        return null;
    }
}
