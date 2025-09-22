package com.labndbnb.landbnb.controller;

import com.labndbnb.landbnb.dto.booking_dto.BookingRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservas")
public class BookingController {

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createReserva(@RequestBody BookingRequest reservaRequest) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @GetMapping("/usuario")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getReservasUsuario(
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @GetMapping("/anfitrion")
    @PreAuthorize("hasRole('ANFITRION')")
    public ResponseEntity<?> getReservasAnfitrion(
            @RequestParam(required = false) Integer alojamientoId,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> cancelarReserva(@PathVariable Integer id) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
