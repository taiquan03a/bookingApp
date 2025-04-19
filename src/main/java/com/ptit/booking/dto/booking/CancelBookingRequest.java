package com.ptit.booking.dto.booking;

import lombok.Data;

@Data
public class CancelBookingRequest {
    private Long bookingId;
    private String reason;
}
