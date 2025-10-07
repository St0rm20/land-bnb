package com.labndbnb.landbnb.dto.user_dto;

import jakarta.validation.constraints.*;

public record UserUpdateDto(
        @Size(max = 100, message = "Name cannot exceed 100 characters")
        String name,

        @Size(max = 100, message = "Last name cannot exceed 100 characters")
        String lastName,

        @Pattern(
                regexp = "^[+]?\\d{7,15}$",
                message = "Phone number must be valid (7–15 digits, optional + at start)"
        )
        String phoneNumber,

        @Size(max = 255, message = "Photo profile URL cannot exceed 255 characters")
        String photoProfile,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description,

        @Size(max = 500, message = "Bio cannot exceed 500 characters")
        String bio,

        @Pattern(
                regexp = "^\\d{4}-\\d{2}-\\d{2}$",
                message = "Date of birth must be in the format YYYY-MM-DD"
        )
        String dateBirth
) {}
