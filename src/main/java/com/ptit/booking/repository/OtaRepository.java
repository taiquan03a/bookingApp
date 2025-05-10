package com.ptit.booking.repository;

import com.ptit.booking.model.Ota;
import com.ptit.booking.model.OtaHotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OtaRepository extends JpaRepository<Ota, Long> {
    @Query("SELECT o FROM OtaHotel oh JOIN oh.ota o WHERE oh.hotel.id = :hotelId")
    List<Ota> findOtaByHotelId(@Param("hotelId") Long hotelId);

    @Query("SELECT oh FROM OtaHotel oh WHERE oh.ota.id = :otaId AND oh.hotel.id = :hotelId")
    OtaHotel findOtaHotel(@Param("otaId") Long otaId, @Param("hotelId") Long hotelId);
}
