package com.labndbnb.landbnb.service.implement;

import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDetailDto;
import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDto;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.mappers.accommodation.AccommodationDetailDtoMapper;
import com.labndbnb.landbnb.mappers.accommodation.AccommodationDtoMapper;
import com.labndbnb.landbnb.model.Accommodation;
import com.labndbnb.landbnb.model.User;
import com.labndbnb.landbnb.repository.AccommodationRepository;
import com.labndbnb.landbnb.service.definition.AccommodationService;
import com.labndbnb.landbnb.service.definition.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class AccommodationServiceImpl implements AccommodationService {
    private final AccommodationRepository accommodationRepository;
    private final AccommodationDtoMapper accommodationDtoMapper;
    private final AccommodationDetailDtoMapper accommodationDetailDtoMapper;
    private final UserService userService;

    private final Integer size = 10;

    @Override
    public Page<AccommodationDto> getAccommodations(Integer page) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Accommodation> accommodations = accommodationRepository.findAll(pageable);
        return accommodations.map(accommodationDtoMapper::toDto);
    }

    @Override
    public InfoDto createAccommodation(AccommodationDetailDto accommodationDetailDto, HttpServletRequest request) throws Exception {

        User user = userService.getUserFromRequest(request);
        if (user == null) {
            throw new Exception("User not found");
        }

        if (!user.getRole().toString().equals("HOST")) {
            return new InfoDto("User is not a host", "The user is not a host");
        }


        Accommodation accommodation = accommodationDetailDtoMapper.toEntity(accommodationDetailDto);
        accommodation.setHost(user);
        accommodation.setAverageRating(BigDecimal.valueOf(0));
        accommodation.setActive(true);
        accommodation.setCreatedAt(LocalDateTime.now());
        accommodation.setUpdatedAt(LocalDateTime.now());

        Accommodation savedAccommodation = accommodationRepository.save(accommodation);

        return new InfoDto("Accommodation created", "The accommodation with ID " + savedAccommodation.getId() + " has been created successfully.");

    }

    @Override
    public AccommodationDetailDto getAccommodation(Long id) throws Exception {


        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new Exception("Accommodation not found"));

        return accommodationDetailDtoMapper.toDto(accommodation);
    }


    @Override
    public void deleteAccommodation(Long id, HttpServletRequest request) throws Exception {
        User user = userService.getUserFromRequest(request);
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Accommodation not found"));
        if (user == null || !Objects.equals(accommodation.getHost().getId(), user.getId())) {
            throw new Exception("You are not the owner of this accommodation");
        }

        accommodationRepository.delete(accommodation);
    }


    @Override
    public AccommodationDetailDto updateAccommodation(AccommodationDetailDto accommodationDetailDto, Long id, HttpServletRequest request) throws Exception {
        User user = userService.getUserFromRequest(request);
        if (user == null || !user.getRole().toString().equals("HOST")) {
            throw new Exception("User is not a host");
        }

        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new Exception("Accommodation not found"));

        if (!Objects.equals(accommodation.getHost().getId(), user.getId())) {
            throw new Exception("You are not the owner of this accommodation");
        }

        accommodationDetailDtoMapper.updateEntityFromDto(accommodationDetailDto, accommodation);

        Accommodation updated = accommodationRepository.save(accommodation);

        return accommodationDetailDtoMapper.toDto(updated);

    }



}
