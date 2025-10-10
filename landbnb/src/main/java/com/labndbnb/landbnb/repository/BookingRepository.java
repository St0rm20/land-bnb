package com.labndbnb.landbnb.repository;

import com.labndbnb.landbnb.model.Booking;
import com.labndbnb.landbnb.model.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByGuestId(Long guestId, Pageable pageable);

    Page<Booking> findByGuestIdAndBookingStatus(Long guestId, BookingStatus status, Pageable pageable);

    Page<Booking> findByAccommodationHostId(Long hostId, Pageable pageable);

    Page<Booking> findByAccommodationHostIdAndBookingStatus(Long hostId, BookingStatus status, Pageable pageable);

    Page<Booking> findByAccommodationIdAndAccommodationHostId(Long accommodationId, Long hostId, Pageable pageable);

    Page<Booking> findByAccommodationIdAndAccommodationHostIdAndBookingStatus(Long accommodationId, Long hostId, BookingStatus status, Pageable pageable);

    Optional<Booking> findByIdAndGuestId(Long bookingId, Long guestId);

}
