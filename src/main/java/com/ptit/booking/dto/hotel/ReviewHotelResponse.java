package com.ptit.booking.dto.hotel;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
public class ReviewHotelResponse {
    private Long reviewId;
    private int hotelPoint;
    private int roomPoint;
    private int locationPoint;
    private int servicePoint;
    private String comment;
    private List<String> image;
}
