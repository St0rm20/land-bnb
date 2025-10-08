package com.labndbnb.landbnb.dto.accommodation_dto;

import com.labndbnb.landbnb.dto.user_dto.UserDto;
import com.labndbnb.landbnb.dto.util_dto.ImageDto;
import jakarta.validation.constraints.*;

import java.util.List;

public record AccommodationDetailDto(
        Integer id,

        @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title cannot exceed 100 characters")
        String title,

        @NotBlank(message = "Description is required")
        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City cannot exceed 100 characters")
        String city,

        @NotBlank(message = "Address is required")
        @Size(max = 200, message = "Address cannot exceed 200 characters")
        String address,

        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be greater than or equal to -90")
        @DecimalMax(value = "90.0", message = "Latitude must be less than or equal to 90")
        Double latitude,

        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be greater than or equal to -180")
        @DecimalMax(value = "180.0", message = "Longitude must be less than or equal to 180")
        Double longitude,

        @NotNull(message = "Price per night is required")
        @Positive(message = "Price per night must be greater than 0")
        Double pricePerNight,

        @NotNull(message = "Max capacity is required")
        @Positive(message = "Max capacity must be greater than 0")
        Integer maxCapacity,

        @NotNull(message = "Services are required")
        List<@NotBlank(message = "Service cannot be blank")
                String> services,

        @NotNull(message = "Host is required")
        UserDto host,

        @DecimalMin(value = "0.0", message = "Average rating cannot be less than 0")
        @DecimalMax(value = "5.0", message = "Average rating cannot be greater than 5")
        Double averageRating,

        @Min(value = 0, message = "Total bookings cannot be negative")
        Integer totalBookings,

        String mainImage,

        List<ImageDto> images
) {}
