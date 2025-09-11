package com.labndbnb.landbnb.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserRegistration {
    private String email;
    private String password;
    private String nombre;
    private String telefono;
    private String rol;
    private LocalDate fechaNacimiento;
}
