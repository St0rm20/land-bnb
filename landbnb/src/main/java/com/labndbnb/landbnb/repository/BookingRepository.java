package com.labndbnb.landbnb.repository;

import com.labndbnb.landbnb.model.Booking;
import com.labndbnb.landbnb.model.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Consultas para huéspedes
    @EntityGraph(attributePaths = {"accommodation", "accommodation.images", "accommodation.services", "accommodation.host"})
    Page<Booking> findByGuestId(Long guestId, Pageable pageable);

    @EntityGraph(attributePaths = {"accommodation", "accommodation.images", "accommodation.services", "accommodation.host"})
    Page<Booking> findByGuestIdAndBookingStatus(Long guestId, BookingStatus status, Pageable pageable);

    // Consultas para anfitriones
    @EntityGraph(attributePaths = {"accommodation", "accommodation.images", "accommodation.services", "accommodation.host"})
    Page<Booking> findByAccommodationHostId(Long hostId, Pageable pageable);

    @EntityGraph(attributePaths = {"accommodation", "accommodation.images", "accommodation.services", "accommodation.host"})
    Page<Booking> findByAccommodationHostIdAndBookingStatus(Long hostId, BookingStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"accommodation", "accommodation.images", "accommodation.services", "accommodation.host"})
    Page<Booking> findByAccommodationIdAndAccommodationHostId(Long accommodationId, Long hostId, Pageable pageable);

    @EntityGraph(attributePaths = {"accommodation", "accommodation.images", "accommodation.services", "accommodation.host"})
    Page<Booking> findByAccommodationIdAndAccommodationHostIdAndBookingStatus(Long accommodationId, Long hostId, BookingStatus status, Pageable pageable);


    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.accommodation a " +
            "JOIN FETCH a.host " +
            "JOIN FETCH b.guest g " +
            "LEFT JOIN FETCH a.images " +
            "LEFT JOIN FETCH a.services " +
            "WHERE b.id = :id")
    Optional<Booking> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.accommodation.id = :accommodationId " +
            "AND b.bookingStatus = :status " +
            "AND (:startDate IS NULL OR b.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR b.createdAt <= :endDate)")
    Long countByAccommodationAndStatus(
            @Param("accommodationId") Long accommodationId,
            @Param("status") BookingStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE b.accommodation.id = :accommodationId " +
            "AND b.bookingStatus = 'CONFIRMED' " +
            "AND (:startDate IS NULL OR b.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR b.createdAt <= :endDate)")
    Double sumRevenueByAccommodation(
            @Param("accommodationId") Long accommodationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT SUM(b.numberOfGuests) FROM Booking b WHERE b.accommodation.id = :accommodationId " +
            "AND b.bookingStatus = 'CONFIRMED' " +
            "AND (:startDate IS NULL OR b.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR b.createdAt <= :endDate)")
    Integer sumGuestsByAccommodation(
            @Param("accommodationId") Long accommodationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    List<Booking> findByAccommodationIdAndStartDateBetween(
            Long accommodationId,
            LocalDateTime start,
            LocalDateTime end
    );


}