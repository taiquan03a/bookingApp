package com.ptit.booking.dto.hotel;

import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class FilterRequest {
    private Long locationId;
    private LocalDate checkin;
    private LocalDate checkout;
    private int adults;
    private int children;
    private int roomNumber;
    Set<Long> amenityIds = new HashSet<>();
    Set<Long> serviceIds = new HashSet<>();
}
