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
            @Mapping(target = "maxCapacity", source = "capacity"),
            @Mapping(target = "pricePerNight", source = "pricePerNight"), // Double → Double
            @Mapping(target = "averageRating", expression = "java(accommodation.getAverageRating() != null ? accommodation.getAverageRating().doubleValue() : null)"),
            @Mapping(target = "mainImage", source = "principalImageUrl"),
            @Mapping(target = "totalBookings", expression = "java(accommodation.getBookings() != null ? accommodation.getBookings().size() : 0)"),
            @Mapping(target = "images", source = "images"), // List<String>
            @Mapping(target = "services", source = "services") // también List<String>
    })
    AccommodationDetailDto toDto(Accommodation accommodation);

    @InheritInverseConfiguration
    @Mappings({
            @Mapping(target = "id", expression = "java(dto.id() != null ? dto.id().longValue() : null)"),
            @Mapping(target = "name", source = "title"),
            @Mapping(target = "capacity", source = "maxCapacity"),
            @Mapping(target = "pricePerNight", source = "pricePerNight"), // Double → Double
            @Mapping(target = "averageRating", expression = "java(dto.averageRating() != null ? java.math.BigDecimal.valueOf(dto.averageRating()) : java.math.BigDecimal.ZERO)"),
            @Mapping(target = "principalImageUrl", source = "mainImage"),
            @Mapping(target = "bookings", ignore = true),
            @Mapping(target = "reviews", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "active", ignore = true)
    })
    Accommodation toEntity(AccommodationDetailDto dto);

    @Named("longToInteger")
    default Integer longToInteger(Long value) {
        return value != null ? value.intValue() : null;
    }
}
