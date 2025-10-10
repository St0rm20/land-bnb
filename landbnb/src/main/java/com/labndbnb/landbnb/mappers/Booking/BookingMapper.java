package com.labndbnb.landbnb.mappers.Booking;

import com.labndbnb.landbnb.dto.booking_dto.BookingDto;
import com.labndbnb.landbnb.mappers.accommodation.AccommodationDetailDtoMapper;
import com.labndbnb.landbnb.model.Booking;
import com.labndbnb.landbnb.mappers.user.UserDtoMapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {AccommodationDetailDtoMapper.class})
public interface BookingMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "checkInDate", source = "startDate")
    @Mapping(target = "checkOutDate", source = "endDate")
    @Mapping(target = "numberOfGuests", source = "numberOfGuests")
    @Mapping(target = "totalPrice", source = "totalPrice")
    @Mapping(target = "status", source = "bookingStatus")
    @Mapping(target = "accommodation", source = "accommodation")
    @Mapping(target = "user", source = "guest")
    BookingDto toDto(Booking booking);
}

