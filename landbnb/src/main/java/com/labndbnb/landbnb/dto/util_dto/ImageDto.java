package com.labndbnb.landbnb.dto.util_dto;

import jakarta.validation.constraints.*;

public record ImageDto(

        @NotBlank(message = "Image URL is required")
        @Pattern(
                regexp = "^(https?:\\/\\/.*)$",
                message = "Image URL must be a valid URL"
        )
        String url
) {}
