package com.labndbnb.landbnb.dto.util_dto;

public record ResponseDTO<T>(
        boolean error,
        T content
) {
}