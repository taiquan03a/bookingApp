package com.ptit.booking.dto.room;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class RoomRequest {
    private Long roomId;
    //private int roomQuantity;
    private int adults;
    private int children;
    private float price;
    private List<Long> serviceIdList;
}
