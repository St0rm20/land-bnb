package com.labndbnb.landbnb.service.definition;

import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDetailDto;
import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDto;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;

import javax.sound.sampled.DataLine;

public interface AccommodationService {

    Page<AccommodationDto> getAccommodations(Integer page) throws Exception;

    InfoDto createAccommodation(AccommodationDetailDto accommodationDetailDto, HttpServletRequest request) throws Exception;

    AccommodationDetailDto getAccommodation(Long id) throws Exception;

    void deleteAccommodation(Long id);

    AccommodationDetailDto updateAccommodation(AccommodationDetailDto accommodationDetailDto, Long id, HttpServletRequest request) throws Exception;
}
