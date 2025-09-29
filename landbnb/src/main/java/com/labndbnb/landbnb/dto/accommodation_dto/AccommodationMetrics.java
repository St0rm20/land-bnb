package com.labndbnb.landbnb.dto.accommodation_dto;

import jakarta.validation.constraints.*;

public record AccommodationMetrics(
        @NotNull(message = "Total reservations is required")
        @Min(value = 0, message = "Total reservations cannot be negative")
        Integer totalReservations,

        @NotNull(message = "Average rating is required")
        @DecimalMin(value = "0.0", message = "Average rating cannot be less than 0")
        @DecimalMax(value = "5.0", message = "Average rating cannot be greater than 5")
        Double averageRating,

        @NotNull(message = "Total revenue is required")
        @PositiveOrZero(message = "Total revenue cannot be negative")
        Double totalRevenue
) {}
