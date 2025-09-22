package com.labndbnb.landbnb.dto.accommodation_dto;

import java.util.List;

public record AccommodationDto(
        String title,
        String description,
        String city,
        String address,
        Double latitude,
        Double longitude,
        Double pricePerNight,
        Integer maxCapacity,
        List<String> services
) {}
