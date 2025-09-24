package com.labndbnb.landbnb.repository;

import com.labndbnb.landbnb.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
