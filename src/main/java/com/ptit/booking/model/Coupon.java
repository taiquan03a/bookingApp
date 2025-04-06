package com.ptit.booking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "coupon")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 255)
    @Column(name = "code")
    private String code;

    @Size(max = 255)
    @Column(name = "discount_type")
    private String discountType;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "max_usage")
    private Integer maxUsage;

    @Column(name = "status")
    private Boolean status;

    @OneToMany(mappedBy = "coupon")
    @JsonIgnore
    private Set<UserCoupon> userCoupons = new LinkedHashSet<>();

    @Column(name = "discount_value", precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "min_booking_amount", precision = 10, scale = 2)
    private BigDecimal minBookingAmount;

    @Column(name = "valid_from_date")
    private LocalDateTime validFromDate;

    @Column(name = "current_usage")
    private Integer currentUsage;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

}