package com.labndbnb.landbnb.service.implement;

import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDetailDto;
import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDto;
import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationMetrics;
import com.labndbnb.landbnb.dto.accommodation_dto.SearchCriteria;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
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
    public InfoDto createAccommodation(AccommodationDetailDto accommodationDetailDto, HttpServletRequest request) throws Exception {

        User user = userService.getUserFromRequest(request);
        if (user == null) {
            throw new Exception("User not found");
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
    public AccommodationDetailDto getAccommodation(Long id) throws Exception {

        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new Exception("Accommodation not found"));

        return accommodationDetailDtoMapper.toDto(accommodation);
    }


    @Override
    public void deleteAccommodation(Long id, HttpServletRequest request) throws Exception {
        User user = userService.getUserFromRequest(request);
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Accommodation not found"));
        if (user == null || !Objects.equals(accommodation.getHost().getId(), user.getId())) {
            throw new Exception("You are not the owner of this accommodation");
        }

        accommodationRepository.delete(accommodation);
    }


    @Override
    public AccommodationDetailDto updateAccommodation(AccommodationDetailDto accommodationDetailDto, Long id, HttpServletRequest request) throws Exception {
        User user = userService.getUserFromRequest(request);
        if (user == null || !user.getRole().toString().equals("HOST")) {
            throw new Exception("User is not a host");
        }

        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new Exception("Accommodation not found"));

        if (!Objects.equals(accommodation.getHost().getId(), user.getId())) {
            throw new Exception("You are not the owner of this accommodation");
        }

        accommodationDetailDtoMapper.updateEntityFromDto(accommodationDetailDto, accommodation);

        Accommodation updated = accommodationRepository.save(accommodation);

        return accommodationDetailDtoMapper.toDto(updated);

    }

    @Override
    public Page<AccommodationDetailDto> searchAccommodations(SearchCriteria criteria, int page) throws Exception {

        PageRequest pageable = PageRequest.of(page, size);

        if (criteria.sortBy() != null) {
            Sort.Direction direction = "desc".equalsIgnoreCase(criteria.sortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC;
            pageable = PageRequest.of(page, size, Sort.by(direction, criteria.sortBy()));
        } else {
            pageable = PageRequest.of(page, size,
                    Sort.by(Sort.Order.desc("averageRating"))
                            .and(Sort.by(Sort.Order.desc("reviews.size")))
                            .and(Sort.by(Sort.Order.desc("createdAt")))
            );
        }


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
    public Page<AccommodationDetailDto> getMyAccommodations(int page, HttpServletRequest request) throws Exception {
        User user = userService.getUserFromRequest(request);
        if (user == null || !user.getRole().toString().equals("HOST")) {
            throw new Exception("User is not a host");
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
            HttpServletRequest request) throws Exception {

        User user = userService.getUserFromRequest(request);

        if (user == null || !user.getRole().toString().equals("HOST")) {
            throw new Exception("User is not a host");
        }

        Accommodation accommodation = accommodationRepository.findById(id.longValue())
                .orElseThrow(() -> new Exception("Accommodation not found"));

        if (!Objects.equals(accommodation.getHost().getId(), user.getId())) {
            throw new Exception("You are not the owner of this accommodation");
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

}