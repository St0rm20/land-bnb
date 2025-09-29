package com.labndbnb.landbnb.dto.util_dto;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int size,
        int number
) {
    public static <T> PageResponse<T> of(List<T> content, long totalElements, int totalPages, int size, int number) {
        return new PageResponse<>(content, totalElements, totalPages, size, number);
    }
}
