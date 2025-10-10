package com.labndbnb.landbnb.repository;

import com.labndbnb.landbnb.model.Accommodation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {

    Page<Accommodation> findByHostId(Long hostId, Pageable pageable);

    @Query("SELECT DISTINCT a FROM Accommodation a " +
            "WHERE (:city IS NULL OR LOWER(a.city) LIKE LOWER(CONCAT('%', :city, '%'))) " +
            "AND (:minPrice IS NULL OR a.pricePerNight >= :minPrice) " +
            "AND (:maxPrice IS NULL OR a.pricePerNight <= :maxPrice) " +
            "AND (:hasWifi IS NULL OR (:hasWifi = true AND 'WIFI' MEMBER OF a.services)) " +
            "AND (:hasPool IS NULL OR (:hasPool = true AND 'PISCINA' MEMBER OF a.services)) " +
            "AND (:allowsPets IS NULL OR (:allowsPets = true AND 'MASCOTAS' MEMBER OF a.services)) " +
            "AND (:hasAirConditioning IS NULL OR (:hasAirConditioning = true AND 'AIRE ACONDICIONADO' MEMBER OF a.services)) " +
            "AND (:hasKitchen IS NULL OR (:hasKitchen = true AND 'COCINA' MEMBER OF a.services)) " +
            "AND (:hasParking IS NULL OR (:hasParking = true AND 'PARKING' MEMBER OF a.services)) " +
            "AND a.active = true " +
            "AND (:checkIn IS NULL OR :checkOut IS NULL OR " +
            "     a.id NOT IN (" +
            "         SELECT b.accommodation.id FROM Booking b " +
            "         WHERE b.bookingStatus IN (com.labndbnb.landbnb.model.enums.BookingStatus.CONFIRMED, com.labndbnb.landbnb.model.enums.BookingStatus.PENDING) " +
            "         AND (b.startDate < :checkOut AND b.endDate > :checkIn)" +
            "     )" +
            ")")
    Page<Accommodation> searchAccommodations(
            @Param("city") String city,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("hasWifi") Boolean hasWifi,
            @Param("hasPool") Boolean hasPool,
            @Param("allowsPets") Boolean allowsPets,
            @Param("hasAirConditioning") Boolean hasAirConditioning,
            @Param("hasKitchen") Boolean hasKitchen,
            @Param("hasParking") Boolean hasParking,
            Pageable pageable
    );
}