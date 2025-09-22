package com.labndbnb.landbnb.dto.booking_dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.labndbnb.landbnb.dto.lodging_dto.LodgingDTO;
import com.labndbnb.landbnb.dto.user_dto.UserDto;

import java.time.LocalDate;

public record BookingDto(
        Integer id,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate fechaCheckIn,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate fechaCheckOut,
        Integer numeroHuespedes,
        Double precioTotal,
        String estado,
        LodgingDTO alojamiento,
        UserDto usuario
) {}
