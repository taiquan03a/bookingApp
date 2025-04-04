package com.ptit.booking.dto.hotelDetail;

import com.ptit.booking.dto.feedback.Comment;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class Feedback {
    private float fiveStar;
    private float fourStar;
    private float threeStar;
    private float twoStar;
    private float oneStar;
    private float ratingHotel;
    private float ratingRoom;
    private float ratingLocation;
    private float ratingService;
    List<Comment> comments;
}
