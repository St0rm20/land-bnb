package com.labndbnb.landbnb.dto.aut_dto;

import jakarta.validation.constraints.*;

public record ResetPasswordRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Reset token is required")
        String token,

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "New password must be at least 8 characters long")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).*$",
                message = "New password must contain at least one uppercase letter, one lowercase letter, and one number"
        )
        String newPassword
) {}
