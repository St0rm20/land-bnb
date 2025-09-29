package com.labndbnb.landbnb.dto.comment_dto;

import jakarta.validation.constraints.*;

public record ReviewRequest(
        @NotNull(message = "Booking ID is required")
        @Positive(message = "Booking ID must be a positive number")
        Integer bookingId,

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating cannot be greater than 5")
        Integer rating,

        @NotBlank(message = "Review text is required")
        @Size(max = 1000, message = "Review text cannot exceed 1000 characters")
        String text
) {}
