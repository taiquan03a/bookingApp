package com.ptit.booking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @Column(name = "check_in", nullable = false)
    private LocalDateTime checkIn;

    @NotNull
    @Column(name = "check_out", nullable = false)
    private LocalDateTime checkOut;

    @Size(max = 50)
    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "room_count")
    private Integer roomCount;

    @ColumnDefault("(now())")
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;


    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "promotion_price", precision = 10, scale = 2)
    private BigDecimal promotionPrice;

    @Column(name = "coupon_price", precision = 10, scale = 2)
    private BigDecimal couponPrice;

    @Column(name = "total_service_price", precision = 10, scale = 2)
    private BigDecimal totalServicePrice;

    @OneToMany(mappedBy = "booking")
    private List<BookingRoom> bookingRooms = new ArrayList<>();

    @OneToMany(mappedBy = "booking")
    private Set<Payment> payments = new LinkedHashSet<>();

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Size(max = 255)
    @Column(name = "customer_name")
    private String customerName;

    @Size(max = 255)
    @Column(name = "customer_phone")
    private String customerPhone;

    @Size(max = 255)
    @Column(name = "customer_email")
    private String customerEmail;

}