package com.labndbnb.landbnb.dto.aut_dto;

import jakarta.validation.constraints.*;

public record AuthResponse(
        @NotBlank(message = "Token is required")
        String token,

        @NotNull(message = "User ID is required")
        Integer userId,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Role is required")
        String role
) {}
