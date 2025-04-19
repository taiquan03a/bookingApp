package com.ptit.booking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "ranks")
public class Rank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @Column(name = "rank_level")
    private Integer rankLevel;

    @Size(max = 50)
    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "min_total_spent", precision = 20, scale = 2)
    private BigDecimal minTotalSpent;

    @Column(name = "min_total_booking")
    private Integer minTotalBooking;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "rank")
    private Set<User> users = new LinkedHashSet<>();

    @OneToMany(mappedBy = "rank")
    @JsonIgnore
    private List<Coupon> coupons = new ArrayList<>();

}