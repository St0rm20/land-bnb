package com.labndbnb.landbnb.dto.accommodation_dto;

import jakarta.validation.constraints.*;

public record AccommodationMetrics(

        @NotNull(message = "Accommodation ID is required")
        @Positive(message = "Accommodation ID must be positive")
        Integer accommodationId,

        @NotBlank(message = "Accommodation name is required")
        @Size(max = 255, message = "Accommodation name cannot exceed 255 characters")
        String accommodationName,

        @NotNull(message = "Total bookings is required")
        @Min(value = 0, message = "Total bookings cannot be negative")
        Integer totalBookings,

        @NotNull(message = "Confirmed bookings is required")
        @Min(value = 0, message = "Confirmed bookings cannot be negative")
        Integer confirmedBookings,

        @NotNull(message = "Cancelled bookings is required")
        @Min(value = 0, message = "Cancelled bookings cannot be negative")
        Integer cancelledBookings,

        @NotNull(message = "Completed bookings is required")
        @Min(value = 0, message = "Completed bookings cannot be negative")
        Integer completedBookings,  

        @NotNull(message = "Pending bookings is required")
        @Min(value = 0, message = "Pending bookings cannot be negative")
        Integer pendingBookings,

        @NotNull(message = "Total revenue is required")
        @DecimalMin(value = "0.0", message = "Total revenue cannot be negative")
        Double totalRevenue,

        @NotNull(message = "Average booking value is required")
        @DecimalMin(value = "0.0", message = "Average booking value cannot be negative")
        Double averageBookingValue,

        @NotNull(message = "Occupancy rate is required")
        @DecimalMin(value = "0.0", message = "Occupancy rate cannot be less than 0")
        @DecimalMax(value = "100.0", message = "Occupancy rate cannot be greater than 100")
        Double occupancyRate,

        @NotNull(message = "Total guests is required")
        @Min(value = 0, message = "Total guests cannot be negative")
        Integer totalGuests,

        @NotNull(message = "Average rating is required")
        @DecimalMin(value = "0.0", message = "Average rating cannot be less than 0")
        @DecimalMax(value = "5.0", message = "Average rating cannot be greater than 5")
        Double averageRating,

        @NotNull(message = "Total reviews is required")
        @Min(value = 0, message = "Total reviews cannot be negative")
        Integer totalReviews



) {}