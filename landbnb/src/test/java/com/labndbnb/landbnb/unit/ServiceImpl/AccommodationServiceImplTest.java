package com.labndbnb.landbnb.unit.ServiceImpl;

import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDetailDto;
import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationDto;
import com.labndbnb.landbnb.dto.accommodation_dto.AccommodationMetrics;
import com.labndbnb.landbnb.dto.accommodation_dto.SearchCriteria;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.mappers.accommodation.AccommodationDetailDtoMapper;
import com.labndbnb.landbnb.mappers.accommodation.AccommodationDtoMapper;
import com.labndbnb.landbnb.model.Accommodation;
import com.labndbnb.landbnb.model.User;
import com.labndbnb.landbnb.model.enums.BookingStatus;
import com.labndbnb.landbnb.model.enums.UserRole;
import com.labndbnb.landbnb.repository.AccommodationRepository;
import com.labndbnb.landbnb.repository.BookingRepository;
import com.labndbnb.landbnb.service.definition.BookingService;
import com.labndbnb.landbnb.service.definition.UserService;
import com.labndbnb.landbnb.service.implement.AccommodationServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Accommodation Service Implementation Unit Tests")
class AccommodationServiceImplTest {

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private AccommodationDtoMapper accommodationDtoMapper;

    @Mock
    private AccommodationDetailDtoMapper accommodationDetailDtoMapper;

    @Mock
    private UserService userService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingService bookingService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AccommodationServiceImpl accommodationService;

    private User testHost;
    private User testGuest;
    private Accommodation testAccommodation;
    private AccommodationDetailDto testAccommodationDetailDto;
    private AccommodationDto testAccommodationDto;

    @BeforeEach
    void setUp() {
        testHost = User.builder()
                .id(1L)
                .email("host@example.com")
                .role(UserRole.HOST)
                .favorites(new ArrayList<>())
                .build();

        testGuest = User.builder()
                .id(2L)
                .email("guest@example.com")
                .role(UserRole.USER)
                .favorites(new ArrayList<>())
                .build();

        testAccommodation = Accommodation.builder()
                .id(1L)
                .name("Test Accommodation")
                .description("Test Description")
                .city("Medellín")
                .address("123 Test St")
                .latitude(BigDecimal.valueOf(6.2442))
                .longitude(BigDecimal.valueOf(-75.5812))
                .pricePerNight(100000.0)
                .capacity(4)
                .services(List.of("WiFi", "Kitchen"))
                .host(testHost)
                .averageRating(BigDecimal.valueOf(4.5))
                .numberOfReviews(10)
                .active(true)
                .reviews(new ArrayList<>())
                .build();

        testAccommodationDetailDto = new AccommodationDetailDto(
                1, "Test Accommodation", "Test Description", "Medellín", "123 Test St",
                6.2442, -75.5812, 100000.0, 4,
                List.of("WiFi", "Kitchen"), null,
                4.5, 10, "https://example.com/image.jpg",
                List.of("https://example.com/img1.jpg")
        );

        testAccommodationDto = new AccommodationDto(
                1, "Test Accommodation", "Test Description", "Medellín", "123 Test St",
                6.2442, -75.5812, 100000.0, 4,
                List.of("WiFi", "Kitchen"), "https://example.com/image.jpg"
        );
    }


    @Nested
    @DisplayName("Get Accommodations")
    class GetAccommodationsTests {

        @Test
        @DisplayName("Should return paginated accommodations when accommodations exist")
        void shouldReturnPaginatedAccommodations_WhenAccommodationsExist() {
            // Given
            int page = 0;
            Pageable pageable = PageRequest.of(page, 10);
            Page<Accommodation> accommodationPage = new PageImpl<>(List.of(testAccommodation));

            when(accommodationRepository.findAll(pageable)).thenReturn(accommodationPage);
            when(accommodationDtoMapper.toDto(testAccommodation)).thenReturn(testAccommodationDto);

            // When
            Page<AccommodationDto> result = accommodationService.getAccommodations(page);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(testAccommodationDto);
            verify(accommodationRepository).findAll(pageable);
            verify(accommodationDtoMapper).toDto(testAccommodation);
        }

        @Test
        @DisplayName("Should return empty page when no accommodations exist")
        void shouldReturnEmptyPage_WhenNoAccommodationsExist() {
            // Given
            int page = 0;
            Pageable pageable = PageRequest.of(page, 10);
            Page<Accommodation> emptyPage = new PageImpl<>(List.of());

            when(accommodationRepository.findAll(pageable)).thenReturn(emptyPage);

            // When
            Page<AccommodationDto> result = accommodationService.getAccommodations(page);

            // Then
            assertThat(result).isEmpty();
            verify(accommodationRepository).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("Create Accommodation")
    class CreateAccommodationTests {

        @Test
        @DisplayName("Should create accommodation when user is host and valid data provided")
        void shouldCreateAccommodation_WhenUserIsHostAndValidDataProvided() throws ExceptionAlert {
            // Given
            when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testHost);
            when(accommodationDetailDtoMapper.toEntity(testAccommodationDetailDto)).thenReturn(testAccommodation);
            when(accommodationRepository.save(any(Accommodation.class))).thenReturn(testAccommodation);

            // When
            InfoDto result = accommodationService.createAccommodation(testAccommodationDetailDto, httpServletRequest);

            // Then
            assertThat(result).isNotNull();
            // Usando el mensaje exacto que devuelve tu código
            assertThat(result.message()).isEqualTo("The accommodation with ID 1 has been created successfully.");
            verify(accommodationRepository).save(any(Accommodation.class));
            verify(accommodationDetailDtoMapper).toEntity(testAccommodationDetailDto);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowException_WhenUserNotFound() throws ExceptionAlert {
            // Given
            when(userService.getUserFromRequest(httpServletRequest)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> accommodationService.createAccommodation(testAccommodationDetailDto, httpServletRequest))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("User not found");
            verify(accommodationRepository, never()).save(any(Accommodation.class));
        }

        @Test
        @DisplayName("Should return info when user is not host")
        void shouldReturnInfo_WhenUserIsNotHost() throws ExceptionAlert {
            // Given
            when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testGuest);

            // When
            InfoDto result = accommodationService.createAccommodation(testAccommodationDetailDto, httpServletRequest);

            // Then
            assertThat(result).isNotNull();
            // Usando el mensaje exacto que devuelve tu código
            assertThat(result.message()).isEqualTo("The user is not a host");
            verify(accommodationRepository, never()).save(any(Accommodation.class));
        }
    }

    @Nested
    @DisplayName("Get Accommodation By ID")
    class GetAccommodationByIdTests {

        @Test
        @DisplayName("Should return accommodation when valid ID provided")
        void shouldReturnAccommodation_WhenValidIdProvided() throws ExceptionAlert {
            // Given
            Long accommodationId = 1L;
            when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(testAccommodation));
            when(accommodationDetailDtoMapper.toDto(testAccommodation)).thenReturn(testAccommodationDetailDto);

            // When
            AccommodationDetailDto result = accommodationService.getAccommodation(accommodationId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(testAccommodationDetailDto);
            verify(accommodationRepository).findById(accommodationId);
            verify(accommodationDetailDtoMapper).toDto(testAccommodation);
        }

        @Test
        @DisplayName("Should throw exception when accommodation not found")
        void shouldThrowException_WhenAccommodationNotFound() {
            // Given
            Long invalidId = 999L;
            when(accommodationRepository.findById(invalidId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> accommodationService.getAccommodation(invalidId))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("Accommodation not found");
        }
    }

    @Nested
    @DisplayName("Delete Accommodation")
    class DeleteAccommodationTests {

        @Test
        @DisplayName("Should delete accommodation when user is owner and no future bookings")
        void shouldDeleteAccommodation_WhenUserIsOwnerAndNoFutureBookings() throws ExceptionAlert {
            // Given
            Long accommodationId = 1L;
            when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testHost);
            when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(testAccommodation));
            when(bookingService.accommodationHasFutureBookings(accommodationId)).thenReturn(false);

            // When
            accommodationService.deleteAccommodation(accommodationId, httpServletRequest);

            // Then
            verify(accommodationRepository).delete(testAccommodation);
            verify(bookingService).accommodationHasFutureBookings(accommodationId);
        }

        @Test
        @DisplayName("Should throw exception when accommodation has future bookings")
        void shouldThrowException_WhenAccommodationHasFutureBookings() throws ExceptionAlert {
            // Given
            Long accommodationId = 1L;
            when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testHost);
            when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(testAccommodation));
            when(bookingService.accommodationHasFutureBookings(accommodationId)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> accommodationService.deleteAccommodation(accommodationId, httpServletRequest))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("Cannot delete accommodation with active bookings");
            verify(accommodationRepository, never()).delete(any(Accommodation.class));
        }

        @Test
        @DisplayName("Should throw exception when user is not owner")
        void shouldThrowException_WhenUserIsNotOwner() throws ExceptionAlert {
            // Given
            Long accommodationId = 1L;
            when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testGuest);
            when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(testAccommodation));

            // When & Then
            assertThatThrownBy(() -> accommodationService.deleteAccommodation(accommodationId, httpServletRequest))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("You are not the owner of this accommodation");
            verify(accommodationRepository, never()).delete(any(Accommodation.class));
            verify(bookingService, never()).accommodationHasFutureBookings(anyLong());
        }

        @Test
        @DisplayName("Should throw exception when accommodation not found")
        void shouldThrowException_WhenAccommodationNotFound() throws ExceptionAlert {
            // Given
            Long accommodationId = 999L;
            when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testHost);
            when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> accommodationService.deleteAccommodation(accommodationId, httpServletRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Accommodation not found");
            verify(bookingService, never()).accommodationHasFutureBookings(anyLong());
        }
    }

    @Nested
    @DisplayName("Update Accommodation")
    class UpdateAccommodationTests {

        @Test
        @DisplayName("Should update accommodation when user is host and owner")
        void shouldUpdateAccommodation_WhenUserIsHostAndOwner() throws ExceptionAlert {
            // Given
            Long accommodationId = 1L;
            when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testHost);
            when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(testAccommodation));
            when(accommodationRepository.save(any(Accommodation.class))).thenReturn(testAccommodation);
            when(accommodationDetailDtoMapper.toDto(testAccommodation)).thenReturn(testAccommodationDetailDto);

            // When
            AccommodationDetailDto result = accommodationService.updateAccommodation(
                    testAccommodationDetailDto, accommodationId, httpServletRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(testAccommodationDetailDto);
            verify(accommodationRepository).save(testAccommodation);
            verify(accommodationDetailDtoMapper).updateEntityFromDto(testAccommodationDetailDto, testAccommodation);
        }

        @Test
        @DisplayName("Should throw exception when user is not host")
        void shouldThrowException_WhenUserIsNotHost() throws ExceptionAlert {
            // Given
            Long accommodationId = 1L;
            when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testGuest);

            // When & Then
            assertThatThrownBy(() -> accommodationService.updateAccommodation(
                    testAccommodationDetailDto, accommodationId, httpServletRequest))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("User is not a host");
            verify(accommodationRepository, never()).save(any(Accommodation.class));
        }

        @Test
        @DisplayName("Should throw exception when accommodation not found")
        void shouldThrowException_WhenAccommodationNotFound() throws ExceptionAlert {
            // Given
            Long accommodationId = 999L;
            when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testHost);
            when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> accommodationService.updateAccommodation(
                    testAccommodationDetailDto, accommodationId, httpServletRequest))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("Accommodation not found");
        }
    }

    @Nested
    @DisplayName("Search Accommodations")
    class SearchAccommodationsTests {

        @Test
        @DisplayName("Should return search results when criteria match")
        void shouldReturnSearchResults_WhenCriteriaMatch() throws ExceptionAlert {
            // Given
            int page = 0;
            SearchCriteria criteria = new SearchCriteria(
                    "Medellín", LocalDate.now().plusDays(1), LocalDate.now().plusDays(5),
                    50000.0, 200000.0, List.of("WiFi"),
                    "price", "asc", true, false, false, false, false, false
            );

            Page<Accommodation> searchPage = new PageImpl<>(List.of(testAccommodation));
            when(accommodationRepository.searchAccommodations(
                    eq("Medellín"), any(LocalDate.class), any(LocalDate.class),
                    eq(50000.0), eq(200000.0), eq(true), eq(false), eq(false),
                    eq(false), eq(false), eq(false), any(Pageable.class)
            )).thenReturn(searchPage);
            when(accommodationDetailDtoMapper.toDto(testAccommodation)).thenReturn(testAccommodationDetailDto);

            // When
            Page<AccommodationDetailDto> result = accommodationService.searchAccommodations(criteria, page);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(accommodationRepository).searchAccommodations(
                    eq("Medellín"), any(LocalDate.class), any(LocalDate.class),
                    eq(50000.0), eq(200000.0), eq(true), eq(false), eq(false),
                    eq(false), eq(false), eq(false), any(Pageable.class)
            );
        }
    }

    @Nested
    @DisplayName("Get My Accommodations")
    class GetMyAccommodationsTests {

        @Test
        @DisplayName("Should return host accommodations when user is host")
        void shouldReturnHostAccommodations_WhenUserIsHost() throws ExceptionAlert {
            // Given
            int page = 0;
            when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testHost);
            Page<Accommodation> hostAccommodations = new PageImpl<>(List.of(testAccommodation));
            when(accommodationRepository.findByHostId(testHost.getId(), PageRequest.of(page, 10)))
                    .thenReturn(hostAccommodations);
            when(accommodationDetailDtoMapper.toDto(testAccommodation)).thenReturn(testAccommodationDetailDto);

            // When
            Page<AccommodationDetailDto> result = accommodationService.getMyAccommodations(page, httpServletRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(accommodationRepository).findByHostId(testHost.getId(), PageRequest.of(page, 10));
        }

        @Test
        @DisplayName("Should throw exception when user is not host")
        void shouldThrowException_WhenUserIsNotHost() throws ExceptionAlert {
            // Given
            int page = 0;
            when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testGuest);

            // When & Then
            assertThatThrownBy(() -> accommodationService.getMyAccommodations(page, httpServletRequest))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("User is not a host");
            verify(accommodationRepository, never()).findByHostId(anyLong(), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Get Accommodation Metrics")
    class GetAccommodationMetricsTests {

        @Test
        @DisplayName("Should return metrics when user is host and owner")
        void shouldReturnMetrics_WhenUserIsHostAndOwner() throws ExceptionAlert {
            // Given
            Integer accommodationId = 1;
            LocalDate startDate = LocalDate.now().minusDays(30);
            LocalDate endDate = LocalDate.now();

            when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testHost);
            when(accommodationRepository.findById(accommodationId.longValue())).thenReturn(Optional.of(testAccommodation));

            // Mock booking repository calls
            when(bookingRepository.countByAccommodationAndStatus(anyLong(), isNull(), any(), any())).thenReturn(10L);
            when(bookingRepository.countByAccommodationAndStatus(anyLong(), eq(BookingStatus.CONFIRMED), any(), any())).thenReturn(8L);
            when(bookingRepository.countByAccommodationAndStatus(anyLong(), eq(BookingStatus.CANCELLED), any(), any())).thenReturn(1L);
            when(bookingRepository.countByAccommodationAndStatus(anyLong(), eq(BookingStatus.PENDING), any(), any())).thenReturn(1L);
            when(bookingRepository.sumRevenueByAccommodation(anyLong(), any(), any())).thenReturn(8000000.0);
            when(bookingRepository.sumGuestsByAccommodation(anyLong(), any(), any())).thenReturn(32);
            when(bookingRepository.findByAccommodationIdAndStartDateBetween(anyLong(), any(), any())).thenReturn(List.of());

            // When
            AccommodationMetrics result = accommodationService.getAccommodationMetrics(
                    accommodationId, startDate, endDate, httpServletRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.accommodationId()).isEqualTo(accommodationId);
            assertThat(result.accommodationName()).isEqualTo("Test Accommodation");
            assertThat(result.totalBookings()).isEqualTo(10);
            assertThat(result.confirmedBookings()).isEqualTo(8);
            verify(accommodationRepository).findById(accommodationId.longValue());
        }

        @Test
        @DisplayName("Should throw exception when user is not host")
        void shouldThrowException_WhenUserIsNotHost() throws ExceptionAlert {
            // Given
            Integer accommodationId = 1;
            LocalDate startDate = LocalDate.now().minusDays(30);
            LocalDate endDate = LocalDate.now();

            when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testGuest);

            // When & Then
            assertThatThrownBy(() -> accommodationService.getAccommodationMetrics(
                    accommodationId, startDate, endDate, httpServletRequest))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("User is not a host"); // Corregido para coincidir con el mensaje real
        }

        @Test
        @DisplayName("Should throw exception when user is not owner")
        void shouldThrowException_WhenUserIsNotOwner() throws ExceptionAlert {
            // Given
            Integer accommodationId = 1;
            LocalDate startDate = LocalDate.now().minusDays(30);
            LocalDate endDate = LocalDate.now();

            User differentHost = User.builder().id(3L).role(UserRole.HOST).build();
            when(userService.getUserFromRequest(httpServletRequest)).thenReturn(differentHost);
            when(accommodationRepository.findById(accommodationId.longValue())).thenReturn(Optional.of(testAccommodation));

            // When & Then
            assertThatThrownBy(() -> accommodationService.getAccommodationMetrics(
                    accommodationId, startDate, endDate, httpServletRequest))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("You are not the owner of this accommodation");
        }
    }

    @Nested
    @DisplayName("Favorite Management")
    class FavoriteManagementTests {

        @Test
        @DisplayName("Should add accommodation to favorites when valid")
        void shouldAddAccommodationToFavorites_WhenValid() throws ExceptionAlert {
            // Given
            Long accommodationId = 1L;
            when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testGuest);
            when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(testAccommodation));
            // No mock para userService.save() ya que es void

            // When
            InfoDto result = accommodationService.addFavorite(accommodationId, httpServletRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.message()).isEqualTo("The accommodation has been added to the user's"); // Mensaje real
            verify(userService).save(any(User.class)); // Solo verificamos que se llame
        }

        @Test
        @DisplayName("Should return info when accommodation already in favorites")
        void shouldReturnInfo_WhenAccommodationAlreadyInFavorites() throws ExceptionAlert {
            // Given
            Long accommodationId = 1L;
            testGuest.getFavorites().add(testAccommodation);
            when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testGuest);
            when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(testAccommodation));

            // When
            InfoDto result = accommodationService.addFavorite(accommodationId, httpServletRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.message()).isEqualTo("The accommodation is already in the user's favorites."); // Mensaje real
            verify(userService, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should remove accommodation from favorites when valid")
        void shouldRemoveAccommodationFromFavorites_WhenValid() {
            // Given
            Long accommodationId = 1L;
            testGuest.getFavorites().add(testAccommodation);
            when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testGuest);
            when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(testAccommodation));
            // No mock para userService.save() ya que es void

            // When
            InfoDto result = accommodationService.removeFavorite(accommodationId, httpServletRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.message()).isEqualTo("The accommodation has been removed from the user's favorites."); // Mensaje real
            verify(userService).save(any(User.class)); // Solo verificamos que se llame
        }

        @Test
        @DisplayName("Should return info when accommodation not in favorites")
        void shouldReturnInfo_WhenAccommodationNotInFavorites() {
            // Given
            Long accommodationId = 1L;
            when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testGuest);
            when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(testAccommodation));

            // When
            InfoDto result = accommodationService.removeFavorite(accommodationId, httpServletRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.message()).isEqualTo("The accommodation is not in the user's favorites."); // Mensaje real
            verify(userService, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should check if accommodation is favorite")
        void shouldCheckIfAccommodationIsFavorite() {
            // Given
            Long accommodationId = 1L;
            testGuest.getFavorites().add(testAccommodation);
            when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testGuest);
            when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(testAccommodation));

            // When
            boolean result = accommodationService.isFavorite(accommodationId, httpServletRequest);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when accommodation is not favorite")
        void shouldReturnFalse_WhenAccommodationIsNotFavorite() {
            // Given
            Long accommodationId = 1L;
            when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testGuest);
            when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(testAccommodation));

            // When
            boolean result = accommodationService.isFavorite(accommodationId, httpServletRequest);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Get Favorite Accommodations")
    class GetFavoriteAccommodationsTests {

        @Test
        @DisplayName("Should return favorite accommodations when user exists")
        void shouldReturnFavoriteAccommodations_WhenUserExists() throws ExceptionAlert {
            // Given
            int page = 0;
            when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testGuest);
            Page<Accommodation> favoritesPage = new PageImpl<>(List.of(testAccommodation));
            when(userService.findFavoritesByUserId(testGuest.getId(), PageRequest.of(page, 10)))
                    .thenReturn(favoritesPage);
            when(accommodationDtoMapper.toDto(testAccommodation)).thenReturn(testAccommodationDto);

            // When
            Page<AccommodationDto> result = accommodationService.getFavoriteAccommodations(page, httpServletRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(userService).findFavoritesByUserId(testGuest.getId(), PageRequest.of(page, 10));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowException_WhenUserNotFound() throws ExceptionAlert {
            // Given
            int page = 0;
            when(userService.getUserFromRequest(httpServletRequest)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> accommodationService.getFavoriteAccommodations(page, httpServletRequest))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("User not found");
        }
    }
}