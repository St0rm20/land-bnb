package com.labndbnb.landbnb.dto.lodging_dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public record SearchCriteria(
        String ciudad,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate checkIn,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate checkOut,
        Integer huespedes,
        Double precioMin,
        Double precioMax,
        List<String> servicios
) {}
