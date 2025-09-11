package com.labndbnb.landbnb.dto;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String email;
    private String nombre;
    private String telefono;
    private String rol;
    private String fotoPerfil;
}
