package com.labndbnb.landbnb.controller;

import com.labndbnb.landbnb.dto.lodging_dto.LodgingDTO;
import com.labndbnb.landbnb.dto.lodging_dto.SearchCriteria;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/alojamientos")
public class LodgingController {

    @GetMapping
    public ResponseEntity<?> getAlojamientos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ANFITRION')")
    public ResponseEntity<?> createAlojamiento(
            @RequestPart LodgingDTO alojamientoDto,
            @RequestPart MultipartFile imagenPrincipal,
            @RequestPart(required = false) List<MultipartFile> imagenes) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAlojamientoById(@PathVariable Integer id) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ANFITRION')")
    public ResponseEntity<?> updateAlojamiento(@PathVariable Integer id, @RequestBody LodgingDTO alojamientoDto) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ANFITRION')")
    public ResponseEntity<?> deleteAlojamiento(@PathVariable Integer id) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/buscar")
    public ResponseEntity<?> searchAlojamientos(
            @RequestBody SearchCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/anfitrion/mis-alojamientos")
    @PreAuthorize("hasRole('ANFITRION')")
    public ResponseEntity<?> getMisAlojamientos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/{id}/metricas")
    @PreAuthorize("hasRole('ANFITRION')")
    public ResponseEntity<?> getAlojamientoMetrics(
            @PathVariable Integer id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}