package com.labndbnb.landbnb.controller;

import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDetailDto;
import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDto;
import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationMetrics;
import com.labndbnb.landbnb.dto.accommodation_dto.SearchCriteria;
import com.labndbnb.landbnb.dto.booking_dto.BookingDatesDto;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.service.definition.AccommodationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accommodations")
public class AccommodationController {

    private final AccommodationService accommodationService;

    @GetMapping
    public ResponseEntity<?> getAccommodations(
            @RequestParam(defaultValue = "0") int page) throws ExceptionAlert {
        Page<AccommodationDto> accommodations = accommodationService.getAccommodations(page);
        return ResponseEntity.status(HttpStatus.OK).body(accommodations);
    }

    @PostMapping
    public ResponseEntity<?> createAccommodation(
            @RequestBody  @Valid AccommodationDetailDto accommodationDto,
            HttpServletRequest request) throws ExceptionAlert {
        InfoDto info = accommodationService.createAccommodation(accommodationDto, request);
        return ResponseEntity.status(HttpStatus.OK).body(info);
    }



    @GetMapping("/{id}")
    public ResponseEntity<?> getAccommodationById(@PathVariable Long id) throws ExceptionAlert{
        AccommodationDetailDto accommodationDetailDto = accommodationService.getAccommodation(id);
        return ResponseEntity.status(HttpStatus.OK).body(accommodationDetailDto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<?> updateAccommodation(
            @PathVariable Long id,
            @RequestBody @Valid AccommodationDetailDto accommodationDetailDto,
            HttpServletRequest request) {

        try {
            AccommodationDetailDto updated =
                    accommodationService.updateAccommodation(accommodationDetailDto, id, request);
            return ResponseEntity.ok(updated);
        } catch (ExceptionAlert e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new InfoDto("Update failed", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<?> deleteAccommodation(@PathVariable Long id, HttpServletRequest request) throws ExceptionAlert
    {
        accommodationService.deleteAccommodation(id, request);
        return ResponseEntity.ok("Accommodation deleted successfully");
    }

    @PostMapping("/search")
    public ResponseEntity<?> searchAccommodations(
            @RequestBody SearchCriteria criteria,
            @RequestParam(defaultValue = "0") int page) throws ExceptionAlert {

        Page<AccommodationDetailDto> results = accommodationService.searchAccommodations(criteria, page);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/host/my-accommodations")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<?> getMyAccommodations(
            @RequestParam(defaultValue = "0") int page,
            HttpServletRequest request) throws ExceptionAlert {

        Page<AccommodationDetailDto> accommodations = accommodationService.getMyAccommodations(page, request);
        return ResponseEntity.ok(accommodations);
    }

    @GetMapping("/{id}/metrics")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<?> getAccommodationMetrics(
            @PathVariable Integer id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest request) throws ExceptionAlert {

        AccommodationMetrics metrics = accommodationService.getAccommodationMetrics(
                id, startDate, endDate, request);
        return ResponseEntity.ok(metrics);
    }


    @GetMapping("/favorites")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getFavoriteAccommodations(
            @RequestParam(defaultValue = "0") int page,
            HttpServletRequest request) throws ExceptionAlert {
        Page<AccommodationDto> favorites = accommodationService.getFavoriteAccommodations(page, request);
        return ResponseEntity.status(HttpStatus.OK).body(favorites);
    }

    @GetMapping("/is-favorite/{accommodationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> isFavorite(
            @PathVariable Long accommodationId,
            HttpServletRequest request) throws ExceptionAlert {
        boolean isFav = accommodationService.isFavorite(accommodationId, request);
        return ResponseEntity.status(HttpStatus.OK).body(isFav);
    }

    @PostMapping("/add-favorite/{accommodationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addFavorite(
            @PathVariable Long accommodationId,
            HttpServletRequest request) throws ExceptionAlert {
        InfoDto info = accommodationService.addFavorite(accommodationId, request);
        return ResponseEntity.status(HttpStatus.OK).body(info);
    }

    @DeleteMapping("/remove-favorite/{accommodationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> removeFavorite(
            @PathVariable Long accommodationId,
            HttpServletRequest request) throws ExceptionAlert {
        InfoDto info = accommodationService.removeFavorite(accommodationId, request);
        return ResponseEntity.status(HttpStatus.OK).body(info);
    }

    @GetMapping("/dates-unavailable/{id}")
    public ResponseEntity<?> getUnavailableDates(@PathVariable Long id) throws ExceptionAlert {
        List<BookingDatesDto> dates = accommodationService.getFutureConfirmedBookingDates(id);
        return ResponseEntity.status(HttpStatus.OK).body(dates);
    }


    @GetMapping("/host/accommodation-favorites/{id_accommodation}")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<?> getUsersWhoFavoritedAccommodation(
            @PathVariable Long id_accommodation,
            HttpServletRequest request) throws ExceptionAlert {
        int users = accommodationService.getUsersWhoFavoritedAccommodation(id_accommodation, request);
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    @PreAuthorize("hasRole('HOST')")
    @GetMapping("/host/{id}")
    public ResponseEntity<com.labndbnb.landbnb.dto.util_dto.ResponseDTO<AccommodationDetailDto>> getAccommodationForHost(
            @PathVariable Long id,
            HttpServletRequest request) {
        try {
            AccommodationDetailDto dto = accommodationService.getHostAccommodation(id, request);
            if (dto == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)

                        .body(new com.labndbnb.landbnb.dto.util_dto.ResponseDTO<AccommodationDetailDto>(true, null));

            }
            return ResponseEntity.ok(new com.labndbnb.landbnb.dto.util_dto.ResponseDTO<AccommodationDetailDto>(false, dto));
        } catch (ExceptionAlert e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new com.labndbnb.landbnb.dto.util_dto.ResponseDTO<AccommodationDetailDto>(true, null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new com.labndbnb.landbnb.dto.util_dto.ResponseDTO<AccommodationDetailDto>(true, null));
        }
    }

}
