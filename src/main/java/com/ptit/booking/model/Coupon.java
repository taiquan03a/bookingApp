package com.ptit.booking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

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
    private Instant expiryDate;

    @Column(name = "max_usage")
    private Integer maxUsage;

    @Column(name = "status")
    private Boolean status;

}