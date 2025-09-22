package com.labndbnb.landbnb.dto.user_dto;

public record UserUpdateDto(
        String nombre,
        String telefono,
        String fotoPerfil,
        String descripcion
) {}
