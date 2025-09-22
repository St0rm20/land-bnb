package com.labndbnb.landbnb.dto.accommodation_dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public record SearchCriteria(
        String city,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate checkIn,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate checkOut,
        Integer guests,
        Double minPrice,
        Double maxPrice,
        List<String> services
) {}
