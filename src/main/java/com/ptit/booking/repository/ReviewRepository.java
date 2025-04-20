package com.ptit.booking.repository;

import com.ptit.booking.model.Hotel;
import com.ptit.booking.model.Review;
import com.ptit.booking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Review findByUserAndHotel(User user, Hotel hotel);
    List<Review> findByHotel(Hotel hotel);

    @Query("select i.url from Image i where i.reviewId = :reviewId and i.type = 'REVIEW'")
    List<String> findImageReview(@Param("reviewId") Long reviewId);
}
