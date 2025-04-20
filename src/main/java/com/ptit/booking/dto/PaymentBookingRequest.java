package com.ptit.booking.dto;

import com.ptit.booking.dto.room.RoomRequest;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PaymentBookingRequest {
    private Long hotelId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Long couponId;
    List<RoomRequest> roomRequestList;
}

