package com.labndbnb.landbnb.dto.booking_dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDto;
import com.labndbnb.landbnb.dto.user_dto.UserDto;

import java.time.LocalDate;

public record BookingDto(
        Integer id,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate checkInDate,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate checkOutDate,
        Integer numberOfGuests,
        Double totalPrice,
        String status,
        AccommodationDto accommodation,
        UserDto user
) {}
