package com.labndbnb.landbnb.dto.aut_dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.labndbnb.landbnb.model.enums.UserRole;
import jakarta.validation.constraints.Email;

import java.time.LocalDate;

public record UserRegistration(
        @Email String email,
        String password,
        String name,
        String phoneNumber,
        UserRole userRole,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate birthDate
) {}