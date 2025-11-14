package com.labndbnb.landbnb.dto.comment_dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentAnswerDto(
        String message,
        Long commentId
) {}
