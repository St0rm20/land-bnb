package com.labndbnb.landbnb.dto.booking_dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDetailDto;
import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDto;
import com.labndbnb.landbnb.dto.user_dto.UserDto;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record BookingDto(
        Integer id,

        @NotNull(message = "Check-in date is required")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate checkInDate,

        @NotNull(message = "Check-out date is required")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate checkOutDate,

        @NotNull(message = "Number of guests is required")
        @Positive(message = "Number of guests must be greater than 0")
        Integer numberOfGuests,

        @NotNull(message = "Total price is required")
        @Positive(message = "Total price must be greater than 0")
        Double totalPrice,

        @NotBlank(message = "Status is required")
        @Size(max = 50, message = "Status cannot exceed 50 characters")
        String status,

        @NotNull(message = "Accommodation is required")
        AccommodationDetailDto accommodation,

        @NotNull(message = "User is required")
        UserDto user
) {}
