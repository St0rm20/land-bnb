package com.labndbnb.landbnb.dto.booking_dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record BookingRequest(
        @NotNull(message = "Accommodation ID is required")
        @Positive(message = "Accommodation ID must be a positive number")
        Integer accommodationId,

        @NotNull(message = "Check-in date is required")
        @FutureOrPresent(message = "Check-in date cannot be in the past")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate checkIn,

        @NotNull(message = "Check-out date is required")
        @Future(message = "Check-out date must be in the future")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate checkOut,

        @NotNull(message = "Number of guests is required")
        @Positive(message = "Number of guests must be greater than 0")
        Integer numberOfGuests
) {}
