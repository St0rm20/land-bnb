package com.labndbnb.landbnb.mappers.accommodation;

import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDto;
import com.labndbnb.landbnb.model.Accommodation;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccommodationDtoMapper {

    @Mappings({
            @Mapping(target = "name", source = "title"),
            @Mapping(target = "capacity", source = "maxCapacity"),
            @Mapping(target = "pricePerNight", expression = "java(dto.pricePerNight() != null ? dto.pricePerNight().intValue() : null)"),
            @Mapping(target = "averageRating", ignore = true),
            @Mapping(target = "principalImageUrl", ignore = true),
            @Mapping(target = "images", ignore = true),
            @Mapping(target = "bookings", ignore = true),
            @Mapping(target = "reviews", ignore = true),
            @Mapping(target = "active", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "host", ignore = true)
    })
    Accommodation toEntity(AccommodationDto dto);

    @InheritInverseConfiguration
    @Mappings({
            @Mapping(target = "title", source = "name"),
            @Mapping(target = "maxCapacity", source = "capacity"),
            @Mapping(target = "pricePerNight", expression = "java(accommodation.getPricePerNight() != null ? accommodation.getPricePerNight().doubleValue() : null)")
    })
    AccommodationDto toDto(Accommodation accommodation);
}
