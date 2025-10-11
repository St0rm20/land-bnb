package com.labndbnb.landbnb.service.definition;

import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDetailDto;
import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDto;

import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationMetrics;
import com.labndbnb.landbnb.dto.accommodation_dto.SearchCriteria;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface AccommodationService {

    Page<AccommodationDto> getAccommodations(Integer page) throws Exception;

    InfoDto createAccommodation(AccommodationDetailDto accommodationDetailDto, HttpServletRequest request) throws Exception;

    AccommodationDetailDto getAccommodation(Long id) throws Exception;

    void deleteAccommodation(Long id, HttpServletRequest request) throws Exception;

    AccommodationDetailDto updateAccommodation(AccommodationDetailDto accommodationDetailDto, Long id, HttpServletRequest request) throws Exception;

    Page<AccommodationDetailDto> searchAccommodations(SearchCriteria criteria, int page) throws Exception;

    Page<AccommodationDetailDto> getMyAccommodations(int page, HttpServletRequest request) throws Exception;

    AccommodationMetrics getAccommodationMetrics(Integer id, LocalDate startDate, LocalDate endDate, HttpServletRequest request) throws Exception;

    InfoDto addFavorite(Long accommodationId, HttpServletRequest request) throws Exception;

    InfoDto removeFavorite(Long accommodationId, HttpServletRequest request);

    Page<AccommodationDto> getFavoriteAccommodations(int page, HttpServletRequest request) throws Exception;

    boolean isFavorite(Long accommodationId, HttpServletRequest request);
}