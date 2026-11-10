package com.labndbnb.landbnb.service.implement;

import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDetailDto;
import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDto;
import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationMetrics;
import com.labndbnb.landbnb.dto.accommodation_dto.SearchCriteria;
import com.labndbnb.landbnb.dto.booking_dto.BookingDatesDto;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.mappers.accommodation.AccommodationDetailDtoMapper;
import com.labndbnb.landbnb.mappers.accommodation.AccommodationDtoMapper;
import com.labndbnb.landbnb.model.Accommodation;
import com.labndbnb.landbnb.model.Booking;
import com.labndbnb.landbnb.model.User;
import com.labndbnb.landbnb.model.enums.BookingStatus;
import com.labndbnb.landbnb.model.enums.UserRole;
import com.labndbnb.landbnb.repository.AccommodationRepository;
import com.labndbnb.landbnb.repository.BookingRepository;
import com.labndbnb.landbnb.service.definition.AccommodationService;
import com.labndbnb.landbnb.service.definition.BookingService;
import com.labndbnb.landbnb.service.definition.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class AccommodationServiceImpl implements AccommodationService {
    private final AccommodationRepository accommodationRepository;
    private final AccommodationDtoMapper accommodationDtoMapper;
    private final AccommodationDetailDtoMapper accommodationDetailDtoMapper;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    private final Integer size = 10;


    @Override
    public Page<AccommodationDto> getAccommodations(Integer page) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Accommodation> accommodations = accommodationRepository.findByActiveTrue(pageable);
        return accommodations.map(accommodationDtoMapper::toDto);
    }

    @Override
    public InfoDto createAccommodation(AccommodationDetailDto accommodationDetailDto, Long userId) throws ExceptionAlert {

        User user = userService.getActiveUser(userId);
        if (user == null) {
            throw new ExceptionAlert("User not found");
        }

        if (user.getRole() != UserRole.HOST) {
            throw new ExceptionAlert("User is not a host");
        }


        Accommodation accommodation = accommodationDetailDtoMapper.toEntity(accommodationDetailDto);
        accommodation.setHost(user);
        accommodation.setAverageRating(BigDecimal.ZERO);
        accommodation.setActive(true);
        accommodation.setNumberOfReviews(0);

        Accommodation savedAccommodation = accommodationRepository.save(accommodation);

        return new InfoDto("Accommodation created", "The accommodation with ID " + savedAccommodation.getId() + " has been created successfully.");

    }

    @Override
    public AccommodationDetailDto getAccommodation(Long id) throws ExceptionAlert {

        Accommodation accommodation = accommodationRepository.findById(id)
                .filter(a -> Boolean.TRUE.equals(a.getActive()))
                .orElseThrow(() -> new ExceptionAlert("Accommodation not found"));

        return accommodationDetailDtoMapper.toDto(accommodation);
    }

    @Override
    public AccommodationDetailDto getHostAccommodation(Long id, Long userId) throws ExceptionAlert {
        User user = userService.getActiveUser(userId);
        if (user == null || !user.getRole().toString().equals("HOST")) {
            throw new ExceptionAlert("User is not a host");
        }

        Accommodation accommodation = accommodationRepository.findById(id)
                .filter(a -> Boolean.TRUE.equals(a.getActive()))
                .orElseThrow(() -> new ExceptionAlert("Accommodation not found"));

        // Esta es la validación clave
        if (!Objects.equals(accommodation.getHost().getId(), user.getId())) {
            throw new ExceptionAlert("You are not the owner of this accommodation");
        }

        return accommodationDetailDtoMapper.toDto(accommodation);
    }

    @Override
    public void deleteAccommodation(Long id, Long userId) throws ExceptionAlert {
        User user = userService.getActiveUser(userId);
        Accommodation accommodation = accommodationRepository.findById(id)
                .filter(a -> Boolean.TRUE.equals(a.getActive()))
                .orElseThrow(() -> new ExceptionAlert("Accommodation not found"));
        if (user == null || !Objects.equals(accommodation.getHost().getId(), user.getId())) {
            throw new ExceptionAlert("You are not the owner of this accommodation");
        }
        if(bookingService.accommodationHasFutureBookings(id)){
            throw new ExceptionAlert("Cannot delete accommodation with active bookings");
        }
        // Borrado lógico: se conserva el historial de reservas y reseñas
        accommodation.setActive(false);
        accommodationRepository.save(accommodation);
    }


    @Override
    public AccommodationDetailDto updateAccommodation(AccommodationDetailDto accommodationDetailDto, Long id, Long userId) throws ExceptionAlert {
        User user = userService.getActiveUser(userId);
        if (user == null || !user.getRole().toString().equals("HOST")) {
            throw new ExceptionAlert("User is not a host");
        }

        Accommodation accommodation = accommodationRepository.findById(id)
                .filter(a -> Boolean.TRUE.equals(a.getActive()))
                .orElseThrow(() -> new ExceptionAlert("Accommodation not found"));

        if (!Objects.equals(accommodation.getHost().getId(), user.getId())) {
            throw new ExceptionAlert("You are not the owner of this accommodation");
        }

        accommodationDetailDtoMapper.updateEntityFromDto(accommodationDetailDto, accommodation);

        Accommodation updated = accommodationRepository.save(accommodation);

        return accommodationDetailDtoMapper.toDto(updated);

    }

    @Override
    public Page<AccommodationDetailDto> searchAccommodations(SearchCriteria criteria, int page) throws ExceptionAlert {

        Sort sort;
        if (criteria.sortBy() != null && !criteria.sortBy().isBlank()) {
            Sort.Direction direction = "desc".equalsIgnoreCase(criteria.sortDirection())
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            // Se mapea el nombre público al de la propiedad de la entidad (lista blanca)
            String property = switch (criteria.sortBy()) {
                case "price" -> "pricePerNight";
                case "rating" -> "averageRating";
                case "name" -> "name";
                case "createdDate" -> "createdAt";
                default -> throw new ExceptionAlert("Invalid sortBy: " + criteria.sortBy());
            };
            sort = Sort.by(direction, property);
        } else {

            sort = Sort.by(Sort.Order.desc("averageRating"), Sort.Order.desc("createdAt"));
        }

        PageRequest pageable = PageRequest.of(page, size, sort);

        Page<Accommodation> accommodations = accommodationRepository.searchAccommodations(
                criteria.city(),
                criteria.checkIn(),
                criteria.checkOut(),
                criteria.minPrice(),
                criteria.maxPrice(),
                criteria.hasWifi(),
                criteria.hasPool(),
                criteria.allowsPets(),
                criteria.hasAirConditioning(),
                criteria.hasKitchen(),
                criteria.hasParking(),
                pageable
        );

        return accommodations.map(accommodationDetailDtoMapper::toDto);
    }

    @Override
    public Page<AccommodationDetailDto> getMyAccommodations(int page, Long userId) throws ExceptionAlert {
        User user = userService.getActiveUser(userId);
        if (user == null || !user.getRole().toString().equals("HOST")) {
            throw new ExceptionAlert("User is not a host");
        }
        PageRequest pageable = PageRequest.of(page, size);

        Page<Accommodation> accommodations = accommodationRepository.findByHostIdAndActiveTrue(user.getId(), pageable);

        return accommodations.map(accommodationDetailDtoMapper::toDto);

    }

    @Override
    public AccommodationMetrics getAccommodationMetrics(
            Integer id,
            LocalDate startDate,
            LocalDate endDate,
            Long userId) throws ExceptionAlert {

        User user = userService.getActiveUser(userId);

        if (user == null || !user.getRole().toString().equals("HOST")) {
            throw new ExceptionAlert("User is not a host");
        }

        Accommodation accommodation = accommodationRepository.findById(id.longValue())
                .orElseThrow(() -> new ExceptionAlert("Accommodation not found"));

        if (!Objects.equals(accommodation.getHost().getId(), user.getId())) {
            throw new ExceptionAlert("You are not the owner of this accommodation");
        }

        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : null;

        Map<BookingStatus, Long> byStatus = new EnumMap<>(BookingStatus.class);
        for (Object[] row : bookingRepository.countByStatusForAccommodation(id.longValue(), start, end)) {
            byStatus.put((BookingStatus) row[0], ((Number) row[1]).longValue());
        }
        Long confirmedBookings = byStatus.getOrDefault(BookingStatus.CONFIRMED, 0L);
        Long cancelledBookings = byStatus.getOrDefault(BookingStatus.CANCELLED, 0L);
        Long pendingBookings = byStatus.getOrDefault(BookingStatus.PENDING, 0L);
        Long completedBookings = byStatus.getOrDefault(BookingStatus.COMPLETED, 0L);
        Long totalBookings = byStatus.values().stream().mapToLong(Long::longValue).sum();

        // ✅ CALCULAR INGRESOS TOTALES
        BigDecimal totalRevenue = bookingRepository.sumRevenueByAccommodation(
                id.longValue(), start, end);
        totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;

        // ✅ CALCULAR TOTAL DE HUÉSPEDES
        Integer totalGuests = bookingRepository.sumGuestsByAccommodation(
                id.longValue(), start, end);
        totalGuests = totalGuests != null ? totalGuests : 0;

        // ✅ CALCULAR VALOR PROMEDIO INCLUYENDO CONFIRMED Y COMPLETED
        Long totalSuccessfulBookings = confirmedBookings + completedBookings;
BigDecimal averageBookingValue = totalSuccessfulBookings > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalSuccessfulBookings), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // ✅ CALCULAR TASA DE OCUPACIÓN INCLUYENDO CONFIRMED Y COMPLETED
        Double occupancyRate = 0.0;
        if (startDate != null && endDate != null) {
            long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
            List<Booking> bookings = bookingRepository.findByAccommodationIdAndStartDateBetween(
                    id.longValue(),
                    start,
                    end
            );

            long bookedDays = bookings.stream()
                    .filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED
                            || b.getBookingStatus() == BookingStatus.COMPLETED)
                    .mapToLong(b -> java.time.temporal.ChronoUnit.DAYS.between(
                            b.getStartDate().toLocalDate(),
                            b.getEndDate().toLocalDate()
                    ))
                    .sum();

            occupancyRate = totalDays > 0 ? (bookedDays * 100.0) / totalDays : 0.0;
        }

        // ✅ RETORNAR MÉTRICAS CON COMPLETEDBOOKINGS
        return new AccommodationMetrics(
                id,
                accommodation.getName(),
                totalBookings.intValue(),
                confirmedBookings.intValue(),
                cancelledBookings.intValue(),
                pendingBookings.intValue(),
                completedBookings.intValue(), // ✅ AGREGAR COMPLETED BOOKINGS
                totalRevenue,
                averageBookingValue,
                occupancyRate,
                totalGuests,
                accommodation.getAverageRating() != null
                        ? accommodation.getAverageRating().doubleValue()
                        : 0.0,
                accommodation.getNumberOfReviews() != null ? accommodation.getNumberOfReviews() : 0
        );
    }

    @Override
    public InfoDto addFavorite(Long accommodationId, Long userId) throws ExceptionAlert {
        User user = userService.getActiveUser(userId);
        if (user == null) {
            return new InfoDto("User not found", "The user could not be found from the request.");
        }
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElse(null);
        if (accommodation == null) {
            return new InfoDto("Accommodation not found", "The accommodation with ID " + accommodationId
                    + " could not be found.");
        }
        if (user.getFavorites().contains(accommodation)) {
            return new InfoDto("Already in favorites", "The accommodation is already in the user's favorites.");
        }
        user.getFavorites().add(accommodation);
        userService.save(user);
        return new InfoDto("Added to favorites", "The accommodation has been added to the user's");
    }

    @Override
    public InfoDto removeFavorite(Long accommodationId, Long userId) throws ExceptionAlert {
        User user = userService.getActiveUser(userId);
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new ExceptionAlert("Accommodation not found"));
        if (!user.getFavorites().contains(accommodation)) {
            return new InfoDto("Not in favorites", "The accommodation is not in the user's favorites.");
        }
        user.getFavorites().remove(accommodation);
        userService.save(user);
        return new InfoDto("Removed from favorites", "The accommodation has been removed from the user's favorites.");
    }

    @Override
    public Page<AccommodationDto> getFavoriteAccommodations(int page, Long userId) throws ExceptionAlert {
        Pageable pageable = PageRequest.of(page, size);
        User user = userService.getActiveUser(userId);
        if (user == null) {
            throw new ExceptionAlert("User not found");
        }
        Page<Accommodation> accommodations = accommodationRepository.findFavoritesByUserId(user.getId(), pageable);
        return accommodations.map(accommodationDtoMapper::toDto);
    }

    @Override
    public boolean isFavorite(Long accommodationId, Long userId) throws ExceptionAlert {
        User user = userService.getActiveUser(userId);
        return accommodationRepository.findById(accommodationId)
                .map(a -> user.getFavorites().contains(a))
                .orElse(false);
    }

    @Override
    public List<BookingDatesDto> getFutureConfirmedBookingDates(Long accommodationId) {
        return bookingService.getFutureConfirmedBookingDates(accommodationId);
    }

    @Override
    public int getUsersWhoFavoritedAccommodation(Long idAccommodation, Long userId) throws ExceptionAlert {
        {
            User user = userService.getActiveUser(userId);
            if (user == null || !user.getRole().toString().equals("HOST")) {
                throw new ExceptionAlert("User is not a host");
            }

            Accommodation accommodation = accommodationRepository.findById(idAccommodation)
                    .orElseThrow(() -> new ExceptionAlert("Accommodation not found"));

            if (!Objects.equals(accommodation.getHost().getId(), user.getId())) {
                throw new ExceptionAlert("You are not the owner of this accommodation");
            }

            Long users = accommodationRepository.countUsersWhoFavoritedAccommodation(idAccommodation);

            return Math.toIntExact(users);
        }
    }

}