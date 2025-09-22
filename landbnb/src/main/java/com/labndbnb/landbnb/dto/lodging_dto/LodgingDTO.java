package com.labndbnb.landbnb.dto.lodging_dto;

// AlojamientoDto.java
import java.util.List;

public record LodgingDTO(
        String titulo,
        String descripcion,
        String ciudad,
        String direccion,
        Double latitud,
        Double longitud,
        Double precioNoche,
        Integer capacidadMaxima,
        List<String> servicios
) {}
