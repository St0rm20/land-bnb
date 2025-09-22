package com.labndbnb.landbnb.dto.comment_dto;

public record CommentRequest(
        Integer reservaId,
        Integer calificacion,
        String texto
) {}