package com.labndbnb.landbnb.dto.accommodation_dto;

import com.labndbnb.landbnb.dto.user_dto.UserDto;
import com.labndbnb.landbnb.dto.util_dto.ImageDto;

import java.util.List;

public record AccommodationDetailDto(
        Integer id,
        String title,
        String description,
        String city,
        String address,
        Double latitude,
        Double longitude,
        Double pricePerNight,
        Integer maxCapacity,
        List<String> services,//amenities
        UserDto host,
        Double averageRating,
        Integer totalBookings,
        String mainImage,
        List<ImageDto> images
) {}