package com.labndbnb.landbnb.dto.aut_dto;

public record AuthResponse(
        String token,
        int userId,
        String email,
        String role
) {}