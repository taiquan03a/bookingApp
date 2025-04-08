package com.ptit.booking.dto.booking;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BookingDetail {
    private String hotelName;
    private String hotelAddress;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
}
