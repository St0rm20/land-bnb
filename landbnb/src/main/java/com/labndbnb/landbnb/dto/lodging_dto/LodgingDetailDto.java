package com.labndbnb.landbnb.dto.lodging_dto;

import com.labndbnb.landbnb.dto.user_dto.UserDto;
import com.labndbnb.landbnb.dto.util_dto.ImageDto;

import java.util.List;

public record LodgingDetailDto(
        Integer id,
        String titulo,
        String descripcion,
        String ciudad,
        String direccion,
        Double latitud,
        Double longitud,
        Double precioNoche,
        Integer capacidadMaxima,
        List<String> servicios,
        UserDto anfitrion,
        Double promedioCalificacion,
        Integer totalReservas,
        String imagenPrincipal,
        List<ImageDto> imagenes
) {}