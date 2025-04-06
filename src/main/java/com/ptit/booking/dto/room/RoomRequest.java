package com.ptit.booking.dto.room;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RoomRequest {
    private Long roomId;
    private int roomQuantity;
    private int adults;
    private int children;
    private float price;
}
