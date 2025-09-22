package com.labndbnb.landbnb.dto.comment_dto;

public record ReviewRequest(
        Integer bookingId,
        Integer rating,
        String text
) {}