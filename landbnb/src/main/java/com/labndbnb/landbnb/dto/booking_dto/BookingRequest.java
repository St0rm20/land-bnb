package com.labndbnb.landbnb.dto.booking_dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record BookingRequest(
        Integer alojamientoId,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate checkIn,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate checkOut,
        Integer numeroHuespedes
) {}