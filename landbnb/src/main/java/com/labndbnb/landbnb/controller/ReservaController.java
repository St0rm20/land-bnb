package com.labndbnb.landbnb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReservaController {

    @PostMapping
    public ResponseEntity<Void> createReserva() {
        return ResponseEntity.status(201).build();
    }

    @GetMapping("/usuario")
    public ResponseEntity<Void> getReservasUsuario() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/anfitrion")
    @PreAuthorize("hasRole('ANFITRION')")
    public ResponseEntity<Void> getReservasAnfitrion() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelarReserva() {
        return ResponseEntity.ok().build();
    }
}
