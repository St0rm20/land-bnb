package com.labndbnb.landbnb.dto.user_dto;

import com.labndbnb.landbnb.model.enums.UserRole;

import java.time.LocalDate;

public record UserDto(
        Integer id,
        String email,
        String name,
        String phoneNumber,
        UserRole userRole,
        String profilePictureUrl,
        LocalDate dateBirth
) {}