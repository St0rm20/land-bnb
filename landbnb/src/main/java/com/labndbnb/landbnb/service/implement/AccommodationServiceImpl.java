package com.labndbnb.landbnb.service.implement;

import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDetailDto;
import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDto;
import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationMetrics;
import com.labndbnb.landbnb.dto.accommodation_dto.SearchCriteria;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.mappers.accommodation.AccommodationDetailDtoMapper;
import com.labndbnb.landbnb.mappers.accommodation.AccommodationDtoMapper;
import com.labndbnb.landbnb.model.Accommodation;
import com.labndbnb.landbnb.model.Booking;
import com.labndbnb.landbnb.model.User;
import com.labndbnb.landbnb.model.enums.BookingStatus;
import com.labndbnb.landbnb.repository.AccommodationRepository;
import com.labndbnb.landbnb.repository.BookingRepository;
import com.labndbnb.landbnb.service.definition.AccommodationService;
import com.labndbnb.landbnb.service.definition.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class AccommodationServiceImpl implements AccommodationService {
    private final AccommodationRepository accommodationRepository;
    private final AccommodationDtoMapper accommodationDtoMapper;
    private final AccommodationDetailDtoMapper accommodationDetailDtoMapper;
    private final UserService userService;
    private final BookingRepository bookingRepository;

    private final Integer size = 10;


    @Override
    public Page<AccommodationDto> getAccommodations(Integer page) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Accommodation> accommodations = accommodationRepository.findAll(pageable);
        return accommodations.map(accommodationDtoMapper::toDto);
    }

    @Override
    public InfoDto createAccommodation(AccommodationDetailDto accommodationDetailDto, HttpServletRequest request) throws ExceptionAlert {

        User user = userService.getUserFromRequest(request);
        if (user == null) {
            throw new ExceptionAlert("User not found");
        }

        if (!user.getRole().toString().equals("HOST")) {
            return new InfoDto("User is not a host", "The user is not a host");
        }


        Accommodation accommodation = accommodationDetailDtoMapper.toEntity(accommodationDetailDto);
        accommodation.setHost(user);
        accommodation.setAverageRating(BigDecimal.valueOf(0));
        accommodation.setActive(true);
        accommodation.setCreatedAt(LocalDateTime.now());
        accommodation.setUpdatedAt(LocalDateTime.now());

        Accommodation savedAccommodation = accommodationRepository.save(accommodation);

        return new InfoDto("Accommodation created", "The accommodation with ID " + savedAccommodation.getId() + " has been created successfully.");

    }

    @Override
    public AccommodationDetailDto getAccommodation(Long id) throws ExceptionAlert {

        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new ExceptionAlert("Accommodation not found"));

        return accommodationDetailDtoMapper.toDto(accommodation);
    }


    @Override
    public void deleteAccommodation(Long id, HttpServletRequest request) throws ExceptionAlert {
        User user = userService.getUserFromRequest(request);
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Accommodation not found"));
        if (user == null || !Objects.equals(accommodation.getHost().getId(), user.getId())) {
            throw new ExceptionAlert("You are not the owner of this accommodation");
        }

        accommodationRepository.delete(accommodation);
    }


    @Override
    public AccommodationDetailDto updateAccommodation(AccommodationDetailDto accommodationDetailDto, Long id, HttpServletRequest request) throws ExceptionAlert {
        User user = userService.getUserFromRequest(request);
        if (user == null || !user.getRole().toString().equals("HOST")) {
            throw new ExceptionAlert("User is not a host");
        }

        Accommodation accommodation = accommodationRepository.findById(id)
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
            sort = Sort.by(direction, criteria.sortBy());
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
    public Page<AccommodationDetailDto> getMyAccommodations(int page, HttpServletRequest request) throws ExceptionAlert {
        User user = userService.getUserFromRequest(request);
        if (user == null || !user.getRole().toString().equals("HOST")) {
            throw new ExceptionAlert("User is not a host");
        }
        PageRequest pageable = PageRequest.of(page, size);

        Page<Accommodation> accommodations = accommodationRepository.findByHostId(Long.valueOf(user.getId()), pageable);

        return accommodations.map(accommodationDetailDtoMapper::toDto);

    }

    @Override
    public AccommodationMetrics getAccommodationMetrics(
            Integer id,
            LocalDate startDate,
            LocalDate endDate,
            HttpServletRequest request) throws ExceptionAlert {

        User user = userService.getUserFromRequest(request);

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

        Long totalBookings = bookingRepository.countByAccommodationAndStatus(
                id.longValue(), null, start, end);
        Long confirmedBookings = bookingRepository.countByAccommodationAndStatus(
                id.longValue(), BookingStatus.CONFIRMED, start, end);
        Long cancelledBookings = bookingRepository.countByAccommodationAndStatus(
                id.longValue(), BookingStatus.CANCELLED, start, end);
        Long pendingBookings = bookingRepository.countByAccommodationAndStatus(
                id.longValue(), BookingStatus.PENDING, start, end);

        Double totalRevenue = bookingRepository.sumRevenueByAccommodation(
                id.longValue(), start, end);
        totalRevenue = totalRevenue != null ? totalRevenue : 0.0;

        Integer totalGuests = bookingRepository.sumGuestsByAccommodation(
                id.longValue(), start, end);
        totalGuests = totalGuests != null ? totalGuests : 0;

        Double averageBookingValue = confirmedBookings > 0
                ? totalRevenue / confirmedBookings
                : 0.0;

        Double occupancyRate = 0.0;
        if (startDate != null && endDate != null) {
            long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
            List<Booking> bookings = bookingRepository.findByAccommodationIdAndStartDateBetween(
                    id.longValue(),
                    start,
                    end
            );

            long bookedDays = bookings.stream()
                    .filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED)
                    .mapToLong(b -> java.time.temporal.ChronoUnit.DAYS.between(
                            b.getStartDate().toLocalDate(),
                            b.getEndDate().toLocalDate()
                    ))
                    .sum();

            occupancyRate = totalDays > 0 ? (bookedDays * 100.0) / totalDays : 0.0;
        }

        return new AccommodationMetrics(
                id,
                accommodation.getName(),
                totalBookings.intValue(),
                confirmedBookings.intValue(),
                cancelledBookings.intValue(),
                pendingBookings.intValue(),
                totalRevenue,
                averageBookingValue,
                occupancyRate,
                totalGuests,
                accommodation.getAverageRating() != null
                        ? accommodation.getAverageRating().doubleValue()
                        : 0.0,
                accommodation.getReviews() != null
                        ? accommodation.getReviews().size()
                        : 0
        );
    }

    @Override
    public InfoDto addFavorite(Long accommodationId, HttpServletRequest request) throws ExceptionAlert {
        User user = userService.getUserFromRequest(request);
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
    public InfoDto removeFavorite(Long accommodationId, HttpServletRequest request) {
        try {
            User user = userService.getUserFromRequest(request);
            if (user == null) {
                return new InfoDto("User not found", "The user could not be found from the request.");
            }
            Accommodation accommodation = accommodationRepository.findById(accommodationId)
                    .orElse(null);
            if (accommodation == null) {
                return new InfoDto("Accommodation not found", "The accommodation with ID " + accommodationId
                        + " could not be found.");
            }
            if (!user.getFavorites().contains(accommodation)) {
                return new InfoDto("Not in favorites", "The accommodation is not in the user's favorites.");
            }
            user.getFavorites().remove(accommodation);
            userService.save(user);
            return new InfoDto("Removed from favorites", "The accommodation has been removed from the user's favorites.");
        } catch (ExceptionAlert e) {
            return new InfoDto("Error", "An error occurred while trying to remove the accommodation from favorites: " + e.getMessage());
        }
    }

    @Override
    public Page<AccommodationDto> getFavoriteAccommodations(int page, HttpServletRequest request) throws ExceptionAlert {
        Pageable pageable = PageRequest.of(page, size);
        User user = userService.getUserFromRequest(request);
        if (user == null) {
            throw new ExceptionAlert("User not found");
        }
        Page<Accommodation> accommodations = userService.findFavoritesByUserId(user.getId(), pageable);
        return accommodations.map(accommodationDtoMapper::toDto);
    }

    @Override
    public boolean isFavorite(Long accommodationId, HttpServletRequest request) {
        try {
            User user = userService.getUserFromRequest(request);
            if (user == null) {
                return false;
            }
            Accommodation accommodation = accommodationRepository.findById(accommodationId)
                    .orElse(null);
            if (accommodation == null) {
                return false;
            }
            return user.getFavorites().contains(accommodation);
        } catch (ExceptionAlert e) {
            return false;
        }
    }

}