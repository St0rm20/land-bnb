package com.labndbnb.landbnb.controller;

import com.labndbnb.landbnb.dto.booking_dto.BookingDto;
import com.labndbnb.landbnb.dto.booking_dto.BookingRequest;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.service.definition.BookingService;
import jakarta.validation.Valid;
import com.labndbnb.landbnb.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final Logger logger = Logger.getLogger(BookingController.class.getName());

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createBooking(
            @RequestBody @Valid BookingRequest reservaRequest,
            @AuthenticationPrincipal User currentUser) throws ExceptionAlert {
        BookingDto booking = bookingService.createBooking(reservaRequest, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getBookingsUser(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser) throws ExceptionAlert {
        Page<BookingDto> bookings = bookingService.getBookingsByUser(status, page, size, currentUser.getId());
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/host")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<?> getBookingsHost(
            @RequestParam(required = false) Integer accommodationId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser) throws ExceptionAlert {
        Page<BookingDto> bookings = bookingService.getBookingsByHost(accommodationId, status, page, size, currentUser.getId());
        return ResponseEntity.ok(bookings);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id, @AuthenticationPrincipal User currentUser) throws ExceptionAlert {
        bookingService.cancelBooking(id, currentUser.getId());
        return ResponseEntity.ok(new InfoDto("Booking cancelled",
                "The booking was successfully cancelled."));
    }


    @PostMapping("/host/{id}/cancel")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<?> cancelBookingByHost(@PathVariable Long id, @AuthenticationPrincipal User currentUser) throws ExceptionAlert {
        bookingService.cancelBookingByHost(id, currentUser.getId());
        return ResponseEntity.ok(new InfoDto("Booking cancelled by host",
                "The booking was successfully cancelled by the host."));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<?> confirmBooking(@PathVariable Long id, @AuthenticationPrincipal User currentUser) throws ExceptionAlert {
         InfoDto info = bookingService.confirmBooking(id, currentUser.getId());
        return ResponseEntity.ok(info);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getBookingById(
            @PathVariable Long id, @AuthenticationPrincipal User currentUser) throws ExceptionAlert {
        BookingDto booking = bookingService.getBookingById(id, currentUser.getId());
        return ResponseEntity.ok(booking);
    }


}
