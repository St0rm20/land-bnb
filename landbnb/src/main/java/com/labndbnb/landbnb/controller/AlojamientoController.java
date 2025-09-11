package com.labndbnb.landbnb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alojamientos")
@RequiredArgsConstructor
public class AlojamientoController {

    @GetMapping
    public ResponseEntity<Void> getAllAlojamientos() {
        // Returns an empty OK response
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Void> getAlojamientoById() {
        // Returns an empty OK response
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ANFITRION')")
    public ResponseEntity<Void> createAlojamiento() {
        // Returns a 201 Created status
        return ResponseEntity.status(201).build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ANFITRION')")
    public ResponseEntity<Void> updateAlojamiento() {
        // Returns an empty OK response
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ANFITRION')")
    public ResponseEntity<Void> deleteAlojamiento() {
        // Returns an empty OK response
        return ResponseEntity.ok().build();
    }

    @PostMapping("/buscar")
    public ResponseEntity<Void> searchAlojamientos() {
        // Returns an empty OK response
        return ResponseEntity.ok().build();
    }

    @GetMapping("/anfitrion/mis-alojamientos")
    @PreAuthorize("hasRole('ANFITRION')")
    public ResponseEntity<Void> getMisAlojamientos() {
        // Returns an empty OK response
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/metricas")
    @PreAuthorize("hasRole('ANFITRION')")
    public ResponseEntity<Void> getMetricasAlojamiento() {
        // Returns an empty OK response
        return ResponseEntity.ok().build();
    }
}

