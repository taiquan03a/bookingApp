package com.ptit.booking.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    @Lob
    @Column(name = "comment")
    private String comment;

    @ColumnDefault("(now())")
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @Column(name = "rating_service")
    private Integer ratingService;

    @Column(name = "rating_hotel")
    private Integer ratingHotel;

    @Column(name = "rating_room")
    private Integer ratingRoom;

    @Column(name = "rating_location")
    private Integer ratingLocation;

}