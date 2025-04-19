package com.ptit.booking.dto.web;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingWebResponse {
    private Long bookingId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime bookingDate;
}
