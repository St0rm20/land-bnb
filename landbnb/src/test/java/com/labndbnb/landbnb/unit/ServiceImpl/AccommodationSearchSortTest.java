package com.labndbnb.landbnb.unit.ServiceImpl;

import com.labndbnb.landbnb.dto.accommodation_dto.SearchCriteria;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.mappers.accommodation.AccommodationDetailDtoMapper;
import com.labndbnb.landbnb.mappers.accommodation.AccommodationDtoMapper;
import com.labndbnb.landbnb.model.Accommodation;
import com.labndbnb.landbnb.repository.AccommodationRepository;
import com.labndbnb.landbnb.repository.BookingRepository;
import com.labndbnb.landbnb.service.definition.BookingService;
import com.labndbnb.landbnb.service.definition.UserService;
import com.labndbnb.landbnb.service.implement.AccommodationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccommodationServiceImpl - ordenamiento de búsqueda")
class AccommodationSearchSortTest {

    @Mock private AccommodationRepository accommodationRepository;
    @Mock private AccommodationDtoMapper accommodationDtoMapper;
    @Mock private AccommodationDetailDtoMapper accommodationDetailDtoMapper;
    @Mock private UserService userService;
    @Mock private BookingRepository bookingRepository;
    @Mock private BookingService bookingService;

    @InjectMocks private AccommodationServiceImpl service;

    private SearchCriteria criteria(String sortBy, String dir) {
        return new SearchCriteria(null, null, null, null, null, null, sortBy, dir,
                null, null, null, null, null, null);
    }

    private Pageable searchAndCapturePageable(SearchCriteria c) throws ExceptionAlert {
        Page<Accommodation> empty = new PageImpl<>(List.of());
        when(accommodationRepository.searchAccommodations(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(Pageable.class))).thenReturn(empty);
        service.searchAccommodations(c, 0);
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(accommodationRepository).searchAccommodations(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), pageable.capture());
        return pageable.getValue();
    }

    @ParameterizedTest(name = "sortBy={0} -> propiedad de entidad {1}")
    @CsvSource({"price,pricePerNight", "rating,averageRating", "name,name", "createdDate,createdAt"})
    @DisplayName("mapea el nombre público a la propiedad real de la entidad")
    void mapsPublicNameToEntityProperty(String sortBy, String property) throws ExceptionAlert {
        Pageable p = searchAndCapturePageable(criteria(sortBy, "desc"));

        Sort.Order order = p.getSort().getOrderFor(property);
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("sin sortBy ordena por rating y fecha de creación descendente")
    void defaultSort() throws ExceptionAlert {
        Pageable p = searchAndCapturePageable(criteria(null, null));

        assertThat(p.getSort().getOrderFor("averageRating")).isNotNull();
        assertThat(p.getSort().getOrderFor("createdAt")).isNotNull();
    }

    @Test
    @DisplayName("un sortBy fuera de la lista blanca se rechaza sin llegar al repositorio")
    void rejectsUnknownSort() {
        assertThatThrownBy(() -> service.searchAccommodations(criteria("password", "asc"), 0))
                .isInstanceOf(ExceptionAlert.class)
                .hasMessageContaining("Invalid sortBy");
        verifyNoInteractions(accommodationRepository);
    }
}
