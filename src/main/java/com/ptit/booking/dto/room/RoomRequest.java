package com.ptit.booking.dto.room;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RoomRequest {
    private Long roomId;
    private int roomQuantity;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int adults;
    private int children;

}
