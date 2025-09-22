package com.labndbnb.landbnb.dto.aut_dto;

public record AuthResponse(
        String token,
        Integer userId,
        String email,
        String rol
) {}