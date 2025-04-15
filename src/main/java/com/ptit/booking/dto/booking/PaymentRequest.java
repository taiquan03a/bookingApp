package com.ptit.booking.dto.booking;

import com.ptit.booking.dto.room.RoomRequest;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PaymentRequest {
    private Long hotelId;
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy", timezone = "Asia/Ho_Chi_Minh")
    private LocalDate checkInDate;
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy", timezone = "Asia/Ho_Chi_Minh")
    private LocalDate checkOutDate;
    private Long couponId;
    List<RoomRequest> roomRequestList;
}
