package com.ptit.booking.dto.hotel;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class HotelHistoryResponse {
    private Long hotelId;
    private Long bookingId;
    private String hotelName;
    private float rating;
    private int feedbackSum;
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "dd-MM-yyyy")
    private LocalDateTime bookingDate;
    private String bookingPrice;
    private String image;
}
