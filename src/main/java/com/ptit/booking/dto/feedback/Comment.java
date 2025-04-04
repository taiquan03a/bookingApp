package com.ptit.booking.dto.feedback;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Comment {
    private String username;
    private String urlAvatar;
    private String comment;
    private int rating;
}
