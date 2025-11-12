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


    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.accommodation a " +
            "JOIN FETCH a.host " +
            "WHERE b.guest.id = :guestId")
    Page<Booking> findByGuestId(@Param("guestId") Long guestId, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.accommodation a " +
            "JOIN FETCH a.host " +
            "WHERE b.guest.id = :guestId AND b.bookingStatus = :bookingStatus")
    Page<Booking> findByGuestIdAndBookingStatus(@Param("guestId") Long guestId,
                                                @Param("bookingStatus") BookingStatus bookingStatus,
                                                Pageable pageable);



    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.accommodation a " +
            "JOIN FETCH a.host " +
            "JOIN FETCH b.guest " +
            "WHERE a.host.id = :hostId")
    Page<Booking> findByAccommodationHostId(@Param("hostId") Long hostId, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.accommodation a " +
            "JOIN FETCH a.host " +
            "JOIN FETCH b.guest " +
            "WHERE a.host.id = :hostId AND b.bookingStatus = :status")
    Page<Booking> findByAccommodationHostIdAndBookingStatus(
            @Param("hostId") Long hostId,
            @Param("status") BookingStatus status,
            Pageable pageable);

    @EntityGraph(attributePaths = {"accommodation", "accommodation.host"})
    Page<Booking> findByAccommodationIdAndAccommodationHostId(Long accommodationId, Long hostId, Pageable pageable);

    @EntityGraph(attributePaths = {"accommodation", "accommodation.host"})
    Page<Booking> findByAccommodationIdAndAccommodationHostIdAndBookingStatus(Long accommodationId, Long hostId, BookingStatus status, Pageable pageable);



    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.accommodation a " +
            "JOIN FETCH a.host " +
            "JOIN FETCH b.guest " +
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



    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.accommodation.id = :accommodationId " +
            "AND b.bookingStatus IN ('CONFIRMED') " +
            "AND (b.startDate < :checkOut AND b.endDate > :checkIn)")
    boolean existsOverlappingBooking(
            @Param("accommodationId") Long accommodationId,
            @Param("checkIn") LocalDateTime checkIn,
            @Param("checkOut") LocalDateTime checkOut
    );

    @Query("SELECT b FROM Booking b " +
            "WHERE b.accommodation.id = :accommodationId " +
            "AND b.bookingStatus IN ('CONFIRMED', 'PENDING') " +
            "AND b.startDate < :checkOut " +
            "AND b.endDate > :checkIn")
    List<Booking> findOverlappingBookings(
            @Param("accommodationId") Long accommodationId,
            @Param("checkIn") LocalDateTime checkIn,
            @Param("checkOut") LocalDateTime checkOut
    );



    List<Booking> findByAccommodationIdAndStartDateBetween(
            Long accommodationId,
            LocalDateTime start,
            LocalDateTime end
    );

    boolean existsByAccommodationIdAndEndDateAfterAndBookingStatusNot(Long accommodationId, LocalDateTime now, BookingStatus bookingStatus);


    /**
     * Obtener reservas futuras confirmadas de un alojamiento
     */
    @Query("SELECT b FROM Booking b WHERE b.accommodation.id = :accommodationId " +
            "AND b.bookingStatus = com.labndbnb.landbnb.model.enums.BookingStatus.CONFIRMED " +
            "AND b.endDate > :currentDate " +
            "ORDER BY b.startDate")
    List<Booking> findFutureConfirmedBookingsByAccommodation(@Param("accommodationId") Long accommodationId,
                                                             @Param("currentDate") LocalDateTime currentDate);

    boolean existsByAccommodation_Id(Long accommodationId);
}
