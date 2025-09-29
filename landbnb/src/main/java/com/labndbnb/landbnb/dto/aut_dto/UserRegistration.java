package com.labndbnb.landbnb.dto.aut_dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.labndbnb.landbnb.model.enums.UserRole;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UserRegistration(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).*$",
                message = "Password must contain at least one uppercase letter, one lowercase letter, and one number"
        )
        String password,

        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name cannot exceed 100 characters")
        String name,

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^[+]?\\d{7,15}$",
                message = "Phone number must be valid (7–15 digits, optional + at start)"
        )
        String phoneNumber,

        @NotNull(message = "User role is required")
        UserRole userRole,

        @NotNull(message = "Birth date is required")
        @Past(message = "Birth date must be in the past")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate birthDate
) {}
