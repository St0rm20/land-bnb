package com.labndbnb.landbnb.dto.util_dto;

import jakarta.validation.constraints.*;

public record ImageCreateDto(
        @NotBlank(message = "Image URL is required")
        @Pattern(
                regexp = "^(https?:\\/\\/.*)$",
                message = "Image URL must be a valid URL"
        )
        String url,

        @NotNull(message = "Main image flag is required")
        Boolean isMain
) {}
