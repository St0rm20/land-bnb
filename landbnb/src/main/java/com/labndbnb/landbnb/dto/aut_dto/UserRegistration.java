package com.labndbnb.landbnb.dto.aut_dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;

import java.time.LocalDate;

public record UserRegistration(
        @Email String email,
        String password,
        String name,
        String phone,
        String role,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate birthDate
) {}