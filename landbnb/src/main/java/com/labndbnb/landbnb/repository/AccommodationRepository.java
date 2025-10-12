package com.labndbnb.landbnb.repository;

import com.labndbnb.landbnb.model.Accommodation;
import com.labndbnb.landbnb.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {

    Page<Accommodation> findByHostId(Long hostId, Pageable pageable);

    @Query("SELECT DISTINCT a FROM Accommodation a " +
            "WHERE a.active = true " +
            "AND (:city IS NULL OR :city = '' OR LOWER(a.city) LIKE LOWER(CONCAT('%', :city, '%'))) " +
            "AND (:minPrice IS NULL OR a.pricePerNight >= :minPrice) " +
            "AND (:maxPrice IS NULL OR a.pricePerNight <= :maxPrice) " +
            "AND (:hasWifi IS NULL OR :hasWifi = false OR 'WiFi' MEMBER OF a.services) " +
            "AND (:hasPool IS NULL OR :hasPool = false OR 'Pool' MEMBER OF a.services) " +
            "AND (:allowsPets IS NULL OR :allowsPets = false OR 'Pets Allowed' MEMBER OF a.services) " +
            "AND (:hasAirConditioning IS NULL OR :hasAirConditioning = false OR 'Air Conditioning' MEMBER OF a.services) " +
            "AND (:hasKitchen IS NULL OR :hasKitchen = false OR 'Kitchen' MEMBER OF a.services) " +
            "AND (:hasParking IS NULL OR :hasParking = false OR 'Parking' MEMBER OF a.services) " +
            "AND (:checkIn IS NULL OR :checkOut IS NULL OR " +
            "     a.id NOT IN (" +
            "         SELECT b.accommodation.id FROM Booking b " +
            "         WHERE b.bookingStatus IN ('CONFIRMED', 'PENDING') " +
            "         AND FUNCTION('DATE', b.startDate) < :checkOut " +
            "         AND FUNCTION('DATE', b.endDate) > :checkIn" +
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

    @Query(
            value = "SELECT COUNT(u) FROM User u JOIN u.favorites a WHERE a.id = :accommodationId"
    )
    Long countUsersWhoFavoritedAccommodation(@Param("accommodationId") Long accommodationId);

}