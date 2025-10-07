package com.labndbnb.landbnb.dto.user_dto;

import com.labndbnb.landbnb.model.enums.UserRole;
import com.labndbnb.landbnb.model.enums.UserStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UserDto(
        Integer id,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name cannot exceed 100 characters")
        String name,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name cannot exceed 100 characters")
        String lastName,

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^[+]?\\d{7,15}$",
                message = "Phone number must be valid (7–15 digits, optional + at start)"
        )
        String phoneNumber,

        @NotNull(message = "User role is required")
        UserRole userRole,

        @Pattern(
                regexp = "^(https?:\\/\\/.*)?$",
                message = "Profile picture must be a valid URL"
        )
        String profilePictureUrl,

        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @NotNull(message = "User status is required")
        UserStatus userStatus,

        @Size(max = 500, message = "Bio cannot exceed 500 characters")
        String bio
) {}
