package com.labndbnb.landbnb.repository;

import com.labndbnb.landbnb.dto.comment_dto.CommentDTO;
import com.labndbnb.landbnb.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> getByAccommodationId(Long accommodation_id,
                                      Pageable pageable);


    Review getByBookingId(Long bookingId);

    List<Review> findByUserId(Integer id);

    boolean existsByUser_IdAndBooking_Id(Integer user_id, Long booking_id);
}
