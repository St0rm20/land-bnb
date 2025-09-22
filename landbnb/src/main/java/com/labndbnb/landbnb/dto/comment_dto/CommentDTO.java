package com.labndbnb.landbnb.dto.comment_dto;

import com.labndbnb.landbnb.dto.user_dto.UserDto;

import java.time.LocalDateTime;

public record CommentDTO (
    Integer id,
    Integer calificacion,
    String texto,
    String respuestaAnfitrion,
    LocalDateTime fechaCreacion,
    UserDto usuario
) {}
