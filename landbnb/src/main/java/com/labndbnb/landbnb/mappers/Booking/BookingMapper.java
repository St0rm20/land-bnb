package com.labndbnb.landbnb.mappers.Booking;

import com.labndbnb.landbnb.dto.booking_dto.BookingDto;
import com.labndbnb.landbnb.mappers.accommodation.AccommodationDetailDtoMapper;
import com.labndbnb.landbnb.model.Booking;
import com.labndbnb.landbnb.mappers.user.UserDtoMapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = {UserDtoMapper.class, AccommodationDetailDtoMapper.class}
)
public interface BookingMapper {

    @Mapping(source = "guest", target = "user")
    @Mapping(source = "accommodation", target = "accommodation")
    @Mapping(source = "bookingStatus", target = "status")
    @Mapping(source = "startDate", target = "checkInDate")
    @Mapping(source = "endDate", target = "checkOutDate")
    BookingDto toDto(Booking booking);
}
