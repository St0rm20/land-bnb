package com.labndbnb.landbnb.service.definition;

import com.labndbnb.landbnb.dto.booking_dto.BookingDto;
import com.labndbnb.landbnb.dto.booking_dto.BookingRequest;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.model.Booking;
import com.labndbnb.landbnb.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;

public interface BookingService {

    BookingDto createBooking(BookingRequest bookingRequest, HttpServletRequest request) throws ExceptionAlert;

    Page<BookingDto> getBookingsByUser(String status, int page, int size, HttpServletRequest request) throws ExceptionAlert;

    Page<BookingDto> getBookingsByHost(Integer accommodationId, String status, int page, int size, HttpServletRequest request) throws ExceptionAlert;

    void cancelBooking(Long id,  HttpServletRequest request) throws ExceptionAlert;

    Booking getBookingById(Long id) throws ExceptionAlert;

    void completeBooking(Long id, HttpServletRequest request);

    void cancelBookingByHost(Long id, HttpServletRequest request);
}
