package com.ptit.booking.dto.hotel;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class UserReviewRequest {
    private Long hotelId;
    private Long bookingId;
    private int hotelPoint;
    private int roomPoint;
    private int locationPoint;
    private int servicePoint;
    private String comment;
    private List<MultipartFile> image;
}
