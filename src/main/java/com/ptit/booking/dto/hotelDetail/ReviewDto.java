package com.ptit.booking.dto.hotelDetail;

import com.ptit.booking.model.Amenity;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class ReviewDto {
    private float rating;
    private int sumReview;
    private String description;
    private Set<Amenity> amenities;
    private String location;
    private String phoneNumber;
    private Feedback feedback;
}
