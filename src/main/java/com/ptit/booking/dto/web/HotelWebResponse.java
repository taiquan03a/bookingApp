package com.ptit.booking.dto.web;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HotelWebResponse {
    private Long hotelId;
    private String hotelName;
    private String hotelAddress;
}
