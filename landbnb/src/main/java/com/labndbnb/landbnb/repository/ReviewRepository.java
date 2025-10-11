package com.labndbnb.landbnb.repository;

import com.labndbnb.landbnb.dto.comment_dto.CommentDTO;
import com.labndbnb.landbnb.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE r.accommodation.id = :accommodationId")
    Page<Review> getByAccommodationId(@Param("accommodationId") Long accommodationId, Pageable pageable);

    Review getByBookingId(Long bookingId);

    List<Review> findByUserId(Long id);

    boolean existsByUser_IdAndBooking_Id(Long user_id, Long booking_id);
}
