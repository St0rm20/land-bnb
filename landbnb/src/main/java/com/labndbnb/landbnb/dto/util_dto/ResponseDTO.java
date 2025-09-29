package com.labndbnb.landbnb.dto.util_dto;

public record ResponseDTO<T>(
        boolean error,
        T content
) {
    public static <T> ResponseDTO<T> success(T content) {
        return new ResponseDTO<>(false, content);
    }

    public static <T> ResponseDTO<T> failure(T content) {
        return new ResponseDTO<>(true, content);
    }
}
