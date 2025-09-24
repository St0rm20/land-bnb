package com.labndbnb.landbnb.repository;

import com.labndbnb.landbnb.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
