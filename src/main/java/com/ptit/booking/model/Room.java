package com.ptit.booking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 100)
    @NotNull
    @Column(name = "room_type", nullable = false, length = 100)
    private String roomType;

    @NotNull
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "room_count")
    private Integer roomCount;

    @ColumnDefault("1")
    @Column(name = "availability")
    private Boolean availability;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @Column(name = "max_adults")
    private Integer maxAdults;

    @Column(name = "max_children")
    private Integer maxChildren;

    @Size(max = 50)
    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "area")
    private Integer area;

    @Size(max = 50)
    @Column(name = "bed", length = 50)
    private String bed;

    @OneToMany(mappedBy = "room")
    @JsonIgnore
    private Set<ServiceRoom> serviceRooms = new LinkedHashSet<>();

    @OneToMany(mappedBy = "room")
    private Set<BookingRoom> bookingRooms = new LinkedHashSet<>();

}