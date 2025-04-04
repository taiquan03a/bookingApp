package com.ptit.booking.repository;

import com.ptit.booking.model.Hotel;
import com.ptit.booking.model.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Set;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> , JpaSpecificationExecutor<Hotel> {
//    @Query("SELECT h FROM Hotel h " +
//            "LEFT JOIN FETCH h.location " +
//            "LEFT JOIN FETCH h.promotions " +
//            "LEFT JOIN FETCH h.reviews")
//    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"location", "promotions", "reviews"})
//    Page<Hotel> findAllWithDetails(@Spec Specification<Hotel> spec, Pageable pageable);
    @EntityGraph(attributePaths = {"location", "promotions", "reviews"})
    Page<Hotel> findAll(Specification<Hotel> spec, Pageable pageable);
}
