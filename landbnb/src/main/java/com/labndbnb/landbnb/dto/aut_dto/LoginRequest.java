package com.labndbnb.landbnb.dto.aut_dto;


import jakarta.validation.constraints.Email;

public record LoginRequest(
        @Email String email,
        String password
) {}

