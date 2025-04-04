package com.ptit.booking.dto.hotelDetail;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SelectRoomRequest {
    private Long hotelId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int roomNumber;
    private int adults;
    private int children;
}
