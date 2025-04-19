package com.ptit.booking.repository;

import com.ptit.booking.model.Rank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RankRepository extends JpaRepository<Rank, Long> {

    @Query("select r from Rank r where r.rankLevel = :rankLevel")
    Rank findByRankLevel(@Param("rankLevel") int rankLevel);

}
