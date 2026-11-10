package com.labndbnb.landbnb.service.definition;

import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDetailDto;
import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDto;

import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationMetrics;
import com.labndbnb.landbnb.dto.accommodation_dto.SearchCriteria;
import com.labndbnb.landbnb.dto.booking_dto.BookingDatesDto;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface AccommodationService {

    Page<AccommodationDto> getAccommodations(Integer page) throws ExceptionAlert;

    InfoDto createAccommodation(AccommodationDetailDto accommodationDetailDto, Long userId) throws ExceptionAlert;

    AccommodationDetailDto getAccommodation(Long id) throws ExceptionAlert;

    void deleteAccommodation(Long id, Long userId) throws ExceptionAlert;

    AccommodationDetailDto updateAccommodation(AccommodationDetailDto accommodationDetailDto, Long id, Long userId) throws ExceptionAlert;

    Page<AccommodationDetailDto> searchAccommodations(SearchCriteria criteria, int page) throws ExceptionAlert;

    Page<AccommodationDetailDto> getMyAccommodations(int page, Long userId) throws ExceptionAlert;

    AccommodationMetrics getAccommodationMetrics(Integer id, LocalDate startDate, LocalDate endDate, Long userId) throws ExceptionAlert;

    InfoDto addFavorite(Long accommodationId, Long userId) throws ExceptionAlert;

    InfoDto removeFavorite(Long accommodationId, Long userId);

    Page<AccommodationDto> getFavoriteAccommodations(int page, Long userId) throws ExceptionAlert;

    boolean isFavorite(Long accommodationId, Long userId);

    List<BookingDatesDto> getFutureConfirmedBookingDates(Long accommodationId);

    int getUsersWhoFavoritedAccommodation(Long idAccommodation, Long userId) throws ExceptionAlert;


    AccommodationDetailDto getHostAccommodation(Long id, Long userId) throws ExceptionAlert;
}