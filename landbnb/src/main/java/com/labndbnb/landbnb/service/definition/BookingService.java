package com.labndbnb.landbnb.service.definition;

import com.labndbnb.landbnb.dto.booking_dto.BookingDatesDto;
import com.labndbnb.landbnb.dto.booking_dto.BookingDto;
import com.labndbnb.landbnb.dto.booking_dto.BookingRequest;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.model.Booking;
import com.labndbnb.landbnb.model.User;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BookingService {

    BookingDto createBooking(BookingRequest bookingRequest, Long userId) throws ExceptionAlert;

    Page<BookingDto> getBookingsByUser(String status, int page, int size, Long userId) throws ExceptionAlert;

    Page<BookingDto> getBookingsByHost(Integer accommodationId, String status, int page, int size, Long userId) throws ExceptionAlert;

    void cancelBooking(Long id,  Long userId) throws ExceptionAlert;

    Booking getBookingById(Long id) throws ExceptionAlert;

    BookingDto getBookingById(Long id, Long userId) throws ExceptionAlert;


    InfoDto confirmBooking(Long id, Long userId) throws ExceptionAlert;

    void cancelBookingByHost(Long id, Long userId) throws ExceptionAlert;

    public boolean accommodationHasFutureBookings(Long accommodationId);

    List<BookingDatesDto> getFutureConfirmedBookingDates(Long accommodationId);

    void autoCompletePastBookings();

}


