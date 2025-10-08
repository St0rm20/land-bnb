package com.labndbnb.landbnb.repository;

import com.labndbnb.landbnb.model.Accommodation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {

    Page<Accommodation> findByHostId(Pageable pageable, Long hostId);

}
