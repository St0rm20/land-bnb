package com.labndbnb.landbnb.repository;

import com.labndbnb.landbnb.model.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {
}
