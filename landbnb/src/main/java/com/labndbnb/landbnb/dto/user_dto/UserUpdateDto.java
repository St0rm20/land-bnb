package com.labndbnb.landbnb.dto.user_dto;

public record UserUpdateDto(
        String name,
        String phoneNumber,
        String photoProfile,
        String description
) {}
