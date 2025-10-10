package com.labndbnb.landbnb.dto.comment_dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentAnswerDto(

        @Size(max = 1000, message = "Text cannot exceed 1000 characters")
        @NotBlank(message = "Text is required")
        String message
) {
}
