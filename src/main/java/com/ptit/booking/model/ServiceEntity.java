package com.ptit.booking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "service")
public class ServiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 50)
    @Column(name = "name", length = 50)
    private String name;

    @Size(max = 50)
    @Column(name = "service_type", length = 50)
    private String serviceType;

    @Size(max = 255)
    @Column(name = "image")
    private String image;

    @Column(name = "description")
    private String description;

    @JsonIgnore
    @OneToMany(mappedBy = "serviceEntity")
    private Set<ServiceRoom> serviceRooms = new LinkedHashSet<>();

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

}