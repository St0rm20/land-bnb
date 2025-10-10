package com.labndbnb.landbnb.mappers.accommodation;

import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDetailDto;
import com.labndbnb.landbnb.model.Accommodation;
import com.labndbnb.landbnb.mappers.user.UserDtoMapper;
import org.mapstruct.*;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {UserDtoMapper.class}
)
public interface AccommodationDetailDtoMapper {

    @Mappings({
            @Mapping(target = "id", source = "id", qualifiedByName = "longToInteger"),
            @Mapping(target = "title", source = "name"),
            @Mapping(target = "description", source = "description"),
            @Mapping(target = "city", source = "city"),
            @Mapping(target = "address", source = "address"),
            @Mapping(target = "latitude", expression = "java(accommodation.getLatitude() != null ? accommodation.getLatitude().doubleValue() : null)"),
            @Mapping(target = "longitude", expression = "java(accommodation.getLongitude() != null ? accommodation.getLongitude().doubleValue() : null)"),
            @Mapping(target = "averageRating", expression = "java(accommodation.getAverageRating() != null ? accommodation.getAverageRating().doubleValue() : null)"),
            @Mapping(target = "pricePerNight", source = "pricePerNight"),
            @Mapping(target = "maxCapacity", source = "capacity"),
            @Mapping(target = "mainImage", source = "principalImageUrl"),
            @Mapping(target = "services", source = "services"),
            @Mapping(target = "images", source = "images"),
            @Mapping(target = "totalBookings", expression = "java(accommodation.getBookings() != null ? accommodation.getBookings().size() : 0)"),
            @Mapping(target = "host", source = "host")
    })
    AccommodationDetailDto toDto(Accommodation accommodation);

    @InheritInverseConfiguration
    @Mappings({
            @Mapping(target = "id", expression = "java(dto.id() != null ? dto.id().longValue() : null)"),
            @Mapping(target = "averageRating", expression = "java(dto.averageRating() != null ? java.math.BigDecimal.valueOf(dto.averageRating()) : java.math.BigDecimal.ZERO)"),
            @Mapping(target = "latitude", expression = "java(dto.latitude() != null ? java.math.BigDecimal.valueOf(dto.latitude()) : null)"),
            @Mapping(target = "longitude", expression = "java(dto.longitude() != null ? java.math.BigDecimal.valueOf(dto.longitude()) : null)"),
            @Mapping(target = "bookings", ignore = true),
            @Mapping(target = "reviews", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "active", ignore = true)
    })
    Accommodation toEntity(AccommodationDetailDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "host", ignore = true),
            @Mapping(target = "bookings", ignore = true),
            @Mapping(target = "reviews", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "active", ignore = true)
    })
    void updateEntityFromDto(AccommodationDetailDto dto, @MappingTarget Accommodation entity);

    @Named("longToInteger")
    default Integer longToInteger(Long value) {
        return value != null ? value.intValue() : null;
    }
}
