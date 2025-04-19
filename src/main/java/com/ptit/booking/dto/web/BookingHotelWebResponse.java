package com.ptit.booking.dto.web;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class BookingHotelWebResponse {
    private String bookingType;
    List<BookingWebResponse> bookings;
}
