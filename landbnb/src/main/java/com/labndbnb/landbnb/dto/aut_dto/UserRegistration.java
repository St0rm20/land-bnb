package com.labndbnb.landbnb.dto.aut_dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;

import java.time.LocalDate;

public record UserRegistration(
        @Email String email,
        String password,
        String nombre,
        String telefono,
        String rol,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate fechaNacimiento
) {}