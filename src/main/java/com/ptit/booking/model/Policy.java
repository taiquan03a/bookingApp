package com.ptit.booking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "policy")
public class Policy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 50)
    @Column(name = "type", length = 50)
    private String type;

    @Size(max = 255)
    @Column(name = "name")
    private String name;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "policy")
    @JsonIgnore
    private Set<HotelPolicy> hotelPolicies = new LinkedHashSet<>();

    @Size(max = 10)
    @Column(name = "`condition`", length = 10)
    private String condition;

    @Size(max = 50)
    @Column(name = "value", length = 50)
    private String value;

    @Size(max = 50)
    @Column(name = "operator", length = 50)
    private String operator;

}