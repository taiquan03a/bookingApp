//package com.ptit.booking.model;
//
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.Setter;
//
//@Getter
//@Setter
//@Entity
//@Table(name = "hotel_promotion")
//public class HotelPromotion {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "my_row_id", nullable = false)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY,optional = false)
//    @JoinColumn(name = "hotel_id")
//    private Hotel hotel;
//
//    @ManyToOne(fetch = FetchType.LAZY,optional = false)
//    @JoinColumn(name = "promotion_id")
//    private Promotion promotion;
//
//}