package com.labndbnb.landbnb.dto.booking_dto;

import java.time.LocalDate;

public record BookingDatesDto(
        LocalDate checkInDate,
        LocalDate checkOutDate
) {
}