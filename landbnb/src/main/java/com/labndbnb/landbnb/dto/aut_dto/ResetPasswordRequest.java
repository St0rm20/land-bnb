package com.labndbnb.landbnb.dto.aut_dto;

public record ResetPasswordRequest(
        String token,
        String newPassword
) {}

