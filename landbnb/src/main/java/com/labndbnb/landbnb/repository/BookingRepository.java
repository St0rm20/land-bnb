package com.labndbnb.landbnb.repository;

import com.labndbnb.landbnb.model.Booking;
import com.labndbnb.landbnb.model.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = {"accommodation", "accommodation.images", "accommodation.services", "accommodation.host"})
    Page<Booking> findByGuestId(Long guestId, Pageable pageable);

    @EntityGraph(attributePaths = {"accommodation", "accommodation.images", "accommodation.services", "accommodation.host"})
    Page<Booking> findByGuestIdAndBookingStatus(Long guestId, BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b " +  "JOIN FETCH b.accommodation a " +  "JOIN FETCH b.guest g " + "WHERE b.id = :id")
    Optional<Booking> findByIdWithDetails(@Param("id") Long id);

    @EntityGraph(attributePaths = {"accommodation", "accommodation.images", "accommodation.services", "accommodation.host"})
    Page<Booking> findByAccommodationHostId(Long hostId, Pageable pageable);

    @EntityGraph(attributePaths = {"accommodation", "accommodation.images", "accommodation.services", "accommodation.host"})
    Page<Booking> findByAccommodationHostIdAndBookingStatus(Long hostId, BookingStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"accommodation", "accommodation.images", "accommodation.services", "accommodation.host"})
    Page<Booking> findByAccommodationIdAndAccommodationHostId(Long accommodationId, Long hostId, Pageable pageable);

    @EntityGraph(attributePaths = {"accommodation", "accommodation.images", "accommodation.services", "accommodation.host"})
    Page<Booking> findByAccommodationIdAndAccommodationHostIdAndBookingStatus(Long accommodationId, Long hostId, BookingStatus status, Pageable pageable);
}
