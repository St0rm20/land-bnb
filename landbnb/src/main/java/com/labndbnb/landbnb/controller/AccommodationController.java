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
            @RequestParam(defaultValue = "0") int page) {
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
    public ResponseEntity<?> getAccommodationById(@PathVariable Integer id) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<?> updateAccommodation(
            @PathVariable Integer id,
            @RequestBody AccommodationDto accommodationDto) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<?> deleteAccommodation(@PathVariable Integer id) {
        return ResponseEntity.status(HttpStatus.OK).build();
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
