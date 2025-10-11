package com.labndbnb.landbnb.dto.comment_dto;

import com.labndbnb.landbnb.dto.user_dto.UserDto;
import com.labndbnb.landbnb.dto.user_dto.UserInfoDto;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record CommentDTO(
        Integer id,

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating cannot be greater than 5")
        Integer calificacion,

        @NotBlank(message = "Text is required")
        @Size(max = 1000, message = "Text cannot exceed 1000 characters")
        String texto,

        @Size(max = 1000, message = "Host reply cannot exceed 1000 characters")
        String respuestaAnfitrion,

        @NotNull(message = "Creation date is required")
        @PastOrPresent(message = "Creation date cannot be in the future")
        LocalDateTime fechaCreacion,

        @NotNull(message = "User is required")
        UserInfoDto usuario
) {}
