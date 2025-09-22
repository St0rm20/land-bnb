package com.labndbnb.landbnb.dto.user_dto;

public record UserDto(
        Integer id,
        String email,
        String nombre,
        String telefono,
        String rol,
        String fotoPerfil
) {}