package com.labndbnb.landbnb.dto.aut_dto;

import jakarta.validation.constraints.*;

public record AuthResponse(
        @NotBlank(message = "Token is required")
        String token

) {}
