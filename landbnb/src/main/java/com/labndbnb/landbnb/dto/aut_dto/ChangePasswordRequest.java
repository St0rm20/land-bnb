package com.labndbnb.landbnb.dto.aut_dto;

public record ChangePasswordRequest(
        String currentPassword,
        String newPassword
) {}