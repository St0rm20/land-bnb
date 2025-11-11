package com.labndbnb.landbnb.mappers.Booking;

import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDetailDto;
import com.labndbnb.landbnb.dto.booking_dto.BookingDto;
import com.labndbnb.landbnb.dto.user_dto.UserInfoDto;
import com.labndbnb.landbnb.mappers.accommodation.AccommodationDetailDtoMapper;
import com.labndbnb.landbnb.model.Booking;
import com.labndbnb.landbnb.model.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import javax.annotation.processing.Generated;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-10T15:59:43-0500",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.3.jar, environment: Java 21.0.8 (BellSoft)"
)
@Component
public class BookingMapperImpl implements BookingMapper {

    @Autowired
    private AccommodationDetailDtoMapper accommodationDetailDtoMapper;
    private final DatatypeFactory datatypeFactory;

    public BookingMapperImpl() {
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        }
        catch ( DatatypeConfigurationException ex ) {
            throw new RuntimeException( ex );
        }
    }

    @Override
    public BookingDto toDto(Booking booking) {
        if ( booking == null ) {
            return null;
        }

        Integer id = null;
        LocalDate checkInDate = null;
        LocalDate checkOutDate = null;
        Integer numberOfGuests = null;
        Double totalPrice = null;
        String status = null;
        AccommodationDetailDto accommodation = null;
        UserInfoDto user = null;

        if ( booking.getId() != null ) {
            id = booking.getId().intValue();
        }
        checkInDate = xmlGregorianCalendarToLocalDate( localDateTimeToXmlGregorianCalendar( booking.getStartDate() ) );
        checkOutDate = xmlGregorianCalendarToLocalDate( localDateTimeToXmlGregorianCalendar( booking.getEndDate() ) );
        numberOfGuests = booking.getNumberOfGuests();
        totalPrice = booking.getTotalPrice();
        if ( booking.getBookingStatus() != null ) {
            status = booking.getBookingStatus().name();
        }
        accommodation = accommodationDetailDtoMapper.toDto( booking.getAccommodation() );
        user = userToUserInfoDto( booking.getGuest() );

        BookingDto bookingDto = new BookingDto( id, checkInDate, checkOutDate, numberOfGuests, totalPrice, status, accommodation, user );

        return bookingDto;
    }

    private XMLGregorianCalendar localDateTimeToXmlGregorianCalendar( LocalDateTime localDateTime ) {
        if ( localDateTime == null ) {
            return null;
        }

        return datatypeFactory.newXMLGregorianCalendar(
            localDateTime.getYear(),
            localDateTime.getMonthValue(),
            localDateTime.getDayOfMonth(),
            localDateTime.getHour(),
            localDateTime.getMinute(),
            localDateTime.getSecond(),
            localDateTime.get( ChronoField.MILLI_OF_SECOND ),
            DatatypeConstants.FIELD_UNDEFINED );
    }

    private static LocalDate xmlGregorianCalendarToLocalDate( XMLGregorianCalendar xcal ) {
        if ( xcal == null ) {
            return null;
        }

        return LocalDate.of( xcal.getYear(), xcal.getMonth(), xcal.getDay() );
    }

    protected UserInfoDto userToUserInfoDto(User user) {
        if ( user == null ) {
            return null;
        }

        String name = null;
        String lastName = null;

        name = user.getName();
        lastName = user.getLastName();

        String photoProfile = null;

        UserInfoDto userInfoDto = new UserInfoDto( name, lastName, photoProfile );

        return userInfoDto;
    }
}
