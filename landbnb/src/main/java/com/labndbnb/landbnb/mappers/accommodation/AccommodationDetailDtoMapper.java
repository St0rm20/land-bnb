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

    @Mapping(target = "id", source = "id", qualifiedByName = "longToInteger")
    @Mapping(target = "title", source = "name")
    @Mapping(target = "latitude", source = "latitude", qualifiedByName = "bigDecimalToDouble")
    @Mapping(target = "longitude", source = "longitude", qualifiedByName = "bigDecimalToDouble")
    @Mapping(target = "averageRating", source = "averageRating", qualifiedByName = "bigDecimalToDouble")
    @Mapping(target = "maxCapacity", source = "capacity")
    @Mapping(target = "mainImage", source = "principalImageUrl")
    @Mapping(target = "totalBookings", source = "bookings", qualifiedByName = "countBookings")
    @Mapping(target = "host", source = "host")
    AccommodationDetailDto toDto(Accommodation accommodation);

    @Mapping(target = "id", source = "id", qualifiedByName = "integerToLong")
    @Mapping(target = "name", source = "title")
    @Mapping(target = "latitude", source = "latitude", qualifiedByName = "doubleToBigDecimal")
    @Mapping(target = "longitude", source = "longitude", qualifiedByName = "doubleToBigDecimal")
    @Mapping(target = "averageRating", source = "averageRating", qualifiedByName = "doubleToBigDecimal")
    @Mapping(target = "capacity", source = "maxCapacity")
    @Mapping(target = "principalImageUrl", source = "mainImage")
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    Accommodation toEntity(AccommodationDetailDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "title")
    @Mapping(target = "capacity", source = "maxCapacity")
    @Mapping(target = "principalImageUrl", source = "mainImage")
    @Mapping(target = "host", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntityFromDto(AccommodationDetailDto dto, @MappingTarget Accommodation entity);

    // Métodos helper
    @Named("longToInteger")
    default Integer longToInteger(Long value) {
        return value != null ? value.intValue() : null;
    }

    @Named("integerToLong")
    default Long integerToLong(Integer value) {
        return value != null ? value.longValue() : null;
    }

    @Named("bigDecimalToDouble")
    default Double bigDecimalToDouble(java.math.BigDecimal value) {
        return value != null ? value.doubleValue() : null;
    }

    @Named("doubleToBigDecimal")
    default java.math.BigDecimal doubleToBigDecimal(Double value) {
        return value != null ? java.math.BigDecimal.valueOf(value) : null;
    }

    @Named("countBookings")
    default Integer countBookings(java.util.List<?> bookings) {
        return bookings != null ? bookings.size() : 0;
    }
}