package com.labndbnb.landbnb.controller;

import com.labndbnb.landbnb.dto.booking_dto.BookingDto;
import com.labndbnb.landbnb.dto.booking_dto.BookingRequest;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.service.definition.BookingService;
import com.labndbnb.landbnb.service.implement.BookingServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/booking")
public class BookingController {

    private final BookingService bookingService;
    static final Logger logger = Logger.getLogger(BookingServiceImpl.class.getName());

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createBooking(
            @RequestBody BookingRequest reservaRequest,
            HttpServletRequest request) {
        try {
            BookingDto booking = bookingService.createBooking(reservaRequest, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(booking);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new InfoDto("Error creating booking", e.getMessage()));
        }
    }

    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getBookingsUser(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        try {
            logger.info("getBookingsUser");
            Page<BookingDto> bookings = bookingService.getBookingsByUser(status, page, size, request);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new InfoDto("Error fetching bookings", e.getMessage()));
        }
    }

    @GetMapping("/host")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<?> getBookingsHost(
            @RequestParam(required = false) Integer accommodationId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        try {
            Page<BookingDto> bookings = bookingService.getBookingsByHost(accommodationId, status, page, size, request);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new InfoDto("Error fetching host bookings", e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id, HttpServletRequest request) {
        try {
            bookingService.cancelBooking(id, request);
            return ResponseEntity.ok(new InfoDto("Booking cancelled", "The booking was successfully cancelled."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new InfoDto("Error cancelling booking", e.getMessage()));
        }
    }


    @PostMapping("/host/{id}/cancel")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<?> cancelBookingByHost(@PathVariable Long id, HttpServletRequest request
    ) {
        try {
            bookingService.cancelBookingByHost(id, request);
            return ResponseEntity.ok(new InfoDto("Booking cancelled by host", "The booking was successfully cancelled by the host."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new InfoDto("Error cancelling booking by host", e.getMessage()));
        }
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> completeBooking(@PathVariable Long id, HttpServletRequest request) {
        try {
            logger.info("Completing booking with ID: " + id);
            bookingService.completeBooking(id, request);
            return ResponseEntity.ok(new InfoDto("Booking completed", "The booking was successfully completed."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new InfoDto("Error completing booking", e.getMessage()));
        }
    }

}
