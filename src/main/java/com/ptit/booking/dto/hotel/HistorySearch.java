package com.ptit.booking.dto.hotel;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class HistorySearch {
    private Long id;
    private String imageUrl;
    private LocalDate searchDate;
    private int aduts;
    private int child;
}
