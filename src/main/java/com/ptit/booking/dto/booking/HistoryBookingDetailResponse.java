package com.ptit.booking.dto.booking;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ptit.booking.dto.room.RoomBooked;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HistoryBookingDetailResponse {
    private String bookingStatus;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime bookingStatusTime;
    private Long bookingId;
    private String hotelName;
    private String hotelAddress;
    private int totalAdults;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime checkIn;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime checkOut;
    private List<RoomBooked> roomBookedList;
    private String couponCode;
    private String totalPriceRoom;
    private String priceCoupon;
    private String totalPriceService;
    private String finalPrice;
    private String paymentDeposit;
    private String paymentRemaining;
    private String reason;
}
