package com.labndbnb.landbnb.service.definition;

import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDetailDto;
import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDto;

import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationMetrics;
import com.labndbnb.landbnb.dto.accommodation_dto.SearchCriteria;
import com.labndbnb.landbnb.dto.booking_dto.BookingDatesDto;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface AccommodationService {

    Page<AccommodationDto> getAccommodations(Integer page) throws ExceptionAlert;

    InfoDto createAccommodation(AccommodationDetailDto accommodationDetailDto, HttpServletRequest request) throws ExceptionAlert;

    AccommodationDetailDto getAccommodation(Long id) throws ExceptionAlert;

    void deleteAccommodation(Long id, HttpServletRequest request) throws ExceptionAlert;

    AccommodationDetailDto updateAccommodation(AccommodationDetailDto accommodationDetailDto, Long id, HttpServletRequest request) throws ExceptionAlert;

    Page<AccommodationDetailDto> searchAccommodations(SearchCriteria criteria, int page) throws ExceptionAlert;

    Page<AccommodationDetailDto> getMyAccommodations(int page, HttpServletRequest request) throws ExceptionAlert;

    AccommodationMetrics getAccommodationMetrics(Integer id, LocalDate startDate, LocalDate endDate, HttpServletRequest request) throws ExceptionAlert;

    InfoDto addFavorite(Long accommodationId, HttpServletRequest request) throws ExceptionAlert;

    InfoDto removeFavorite(Long accommodationId, HttpServletRequest request);

    Page<AccommodationDto> getFavoriteAccommodations(int page, HttpServletRequest request) throws ExceptionAlert;

    boolean isFavorite(Long accommodationId, HttpServletRequest request);

    List<BookingDatesDto> getFutureConfirmedBookingDates(Long accommodationId);

}