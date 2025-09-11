package com.labndbnb.landbnb.model;

import lombok.Data;
import java.time.LocalDate;


@Data
public class User {

    private Long id;
    private String email;
    private String password;
    private String nombre;
    private String telefono;
    private String rol;
    private LocalDate fechaNacimiento;
    private String fotoPerfil;
    private String descripcion;

}

