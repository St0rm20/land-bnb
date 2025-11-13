package com.labndbnb.landbnb.mappers.accommodation;

import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDto;
import com.labndbnb.landbnb.model.Accommodation;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.math.BigDecimal;

/**
 * Mapper para convertir entre la entidad Accommodation y AccommodationDto.
 * Asume que es un componente de Spring (@Mapper(componentModel = "spring")).
 */
@Mapper(componentModel = "spring")
public interface AccommodationDtoMapper {

    @Mappings({
            // === Renombrar campos ===
            // DTO.title -> Entity.name
            @Mapping(target = "name", source = "title"),
            // DTO.maxCapacity -> Entity.capacity
            @Mapping(target = "capacity", source = "maxCapacity"),
            // DTO.mainImage -> Entity.principalImageUrl
            @Mapping(target = "principalImageUrl", source = "mainImage"),

            // === Conversión de tipo explícita (DTO -> Entity) ===
            // DTO.latitude (Double) -> Entity.latitude (BigDecimal)
            @Mapping(target = "latitude", expression = "java(dto.latitude() != null ? new java.math.BigDecimal(dto.latitude().toString()) : null)"),
            // DTO.longitude (Double) -> Entity.longitude (BigDecimal)
            @Mapping(target = "longitude", expression = "java(dto.longitude() != null ? new java.math.BigDecimal(dto.longitude().toString()) : null)"),

            // === Ignorar campos de la Entidad ===
            // Estos campos no deben ser mapeados desde el DTO,
            // ya que son calculados, gestionados por la BD o relaciones.
            @Mapping(target = "averageRating", ignore = true),
            @Mapping(target = "images", ignore = true),
            @Mapping(target = "bookings", ignore = true),
            @Mapping(target = "reviews", ignore = true),
            @Mapping(target = "active", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "host", ignore = true),
            @Mapping(target = "numberOfReviews", ignore = true),
            @Mapping(target = "usersWhoFavorited", ignore = true)
    })
    Accommodation toEntity(AccommodationDto dto);

    @InheritInverseConfiguration // Invierte los mapeos de toEntity
    @Mappings({
            // === Sobrescribir conversiones de tipo (Entity -> DTO) ===
            // Entity.latitude (BigDecimal) -> DTO.latitude (Double)
            @Mapping(target = "latitude", expression = "java(accommodation.getLatitude() != null ? accommodation.getLatitude().doubleValue() : null)"),
            // Entity.longitude (BigDecimal) -> DTO.longitude (Double)
            @Mapping(target = "longitude", expression = "java(accommodation.getLongitude() != null ? accommodation.getLongitude().doubleValue() : null)")

            // Nota: Los mapeos 'name' -> 'title', 'capacity' -> 'maxCapacity',
            // y 'principalImageUrl' -> 'mainImage' se heredan y se invierten automáticamente.
            // Los campos con nombres idénticos (id, description, city, address, pricePerNight, services)
            // se mapean automáticamente en ambas direcciones.
    })
    AccommodationDto toDto(Accommodation accommodation);
}