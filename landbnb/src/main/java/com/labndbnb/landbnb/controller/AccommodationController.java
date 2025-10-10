package com.labndbnb.landbnb.controller;

import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDetailDto;
import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDto;
import com.labndbnb.landbnb.dto.accommodation_dto.SearchCriteria;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
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
            @RequestParam(defaultValue = "0") int page) throws Exception {
        Page<AccommodationDto> accommodations = accommodationService.getAccommodations(page);
        return ResponseEntity.status(HttpStatus.OK).body(accommodations);
    }

    @PostMapping
    public ResponseEntity<?> createAccommodation(
            @RequestBody  @Valid AccommodationDetailDto accommodationDto,
            HttpServletRequest request) throws Exception {
        InfoDto info = accommodationService.createAccommodation(accommodationDto, request);
        return ResponseEntity.status(HttpStatus.OK).body(info);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAccommodationById(@PathVariable Long id) throws Exception{
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
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new InfoDto("Update failed", e.getMessage()));
        }
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<?> deleteAccommodation(@PathVariable Long id, HttpServletRequest request) throws Exception {
        accommodationService.deleteAccommodation(id, request);
        return ResponseEntity.ok("Accommodation deleted successfully");
    }

    @PostMapping("/search")
    public ResponseEntity<?> searchAccommodations(
            @RequestBody SearchCriteria criteria,
            @RequestParam(defaultValue = "0") int page) {

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/host/my-accommodations")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<?> getMyAccommodations(
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    //getAccommodationsByHostId

    @GetMapping("/{id}/metrics")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<?> getAccommodationMetrics(
            @PathVariable Integer id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
