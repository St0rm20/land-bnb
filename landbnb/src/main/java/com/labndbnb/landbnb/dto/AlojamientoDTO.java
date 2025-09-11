package com.labndbnb.landbnb.dto;


import lombok.Data;
import java.util.List;

@Data
public class AlojamientoDetailDto {
    private Long id;
    private String titulo;
    private String descripcion;
    private String ciudad;
    private String direccion;
    private Double latitud;
    private Double longitud;
    private Double precioNoche;
    private Integer capacidadMaxima;
    private List<String> servicios;
    private UserDto anfitrion;
    private Double promedioCalificacion;
    private Integer totalReservas;
    private String imagenPrincipal;
    private List<ImagenDto> imagenes;
}
