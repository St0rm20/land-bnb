package com.labndbnb.landbnb.unit.ServiceImpl;

import com.labndbnb.landbnb.dto.booking_dto.BookingDatesDto;
import com.labndbnb.landbnb.dto.booking_dto.BookingDto;
import com.labndbnb.landbnb.dto.booking_dto.BookingRequest;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.mappers.Booking.BookingMapper;
import com.labndbnb.landbnb.model.Accommodation;
import com.labndbnb.landbnb.model.Booking;
import com.labndbnb.landbnb.model.User;
import com.labndbnb.landbnb.model.enums.BookingStatus;
import com.labndbnb.landbnb.model.enums.UserRole;
import com.labndbnb.landbnb.repository.AccommodationRepository;
import com.labndbnb.landbnb.repository.BookingRepository;
import com.labndbnb.landbnb.service.definition.UserService;
import com.labndbnb.landbnb.service.implement.BookingServiceImpl;
import com.labndbnb.landbnb.service.implement.MailServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Booking Service Implementation Unit Tests")
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private UserService userService;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private MailServiceImpl mailServiceImpl;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User testGuest;
    private User testHost;
    private Accommodation testAccommodation;
    private BookingRequest testBookingRequest;
    private Booking testBooking;
    private BookingDto testBookingDto;

    @BeforeEach
    void setUp() {
        testHost = User.builder()
                .id(1L)
                .email("host@example.com")
                .role(UserRole.HOST)
                .name("Host Name")
                .lastName("Host Last Name")
                .build();

        testGuest = User.builder()
                .id(2L)
                .email("guest@example.com")
                .role(UserRole.USER)
                .name("Guest Name")
                .lastName("Guest Last Name")
                .build();

        testAccommodation = Accommodation.builder()
                .id(10L)
                .name("Test Accommodation")
                .pricePerNight(100.0)
                .host(testHost)
                .build();

        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = LocalDate.now().plusDays(10);

        testBookingRequest = new BookingRequest(
                10, checkIn, checkOut, 2
        );

        testBooking = Booking.builder()
                .id(1L)
                .guest(testGuest)
                .accommodation(testAccommodation)
                .startDate(checkIn.atStartOfDay())
                .endDate(checkOut.atStartOfDay())
                .totalPrice(500.0) // 5 days * 100.0
                .numberOfGuests(2)
                .bookingCode(UUID.randomUUID().toString().substring(0, 12).toUpperCase())
                .createdAt(LocalDateTime.now())
                .bookingStatus(BookingStatus.PENDING)
                .build();

    }

    //-------------------------------------------------------------------------
    // NESTED CLASS: CREATE BOOKING TESTS
    //-------------------------------------------------------------------------

    @Nested
    @DisplayName("Create Booking")
    class CreateBookingTests {

        @Test
        @DisplayName("Should create and return booking when dates are valid and available")
        void shouldCreateAndReturnBooking_WhenDatesValidAndAvailable() throws ExceptionAlert {
            // Given
            when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testGuest);
            when(accommodationRepository.findById(testAccommodation.getId())).thenReturn(Optional.of(testAccommodation));
            when(bookingRepository.existsOverlappingBooking(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(false);
            when(bookingRepository.findOverlappingBookings(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(Collections.emptyList());
            when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
            when(bookingMapper.toDto(testBooking)).thenReturn(testBookingDto);

            // When
            BookingDto result = bookingService.createBooking(testBookingRequest, httpServletRequest);

            // Then
            assertThat(result).isEqualTo(testBookingDto);

            ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
            verify(bookingRepository).save(bookingCaptor.capture());

            Booking capturedBooking = bookingCaptor.getValue();
            assertThat(capturedBooking.getGuest().getId()).isEqualTo(testGuest.getId());
            assertThat(capturedBooking.getTotalPrice()).isEqualTo(500.0); // 5 days * 100.0
            assertThat(capturedBooking.getBookingStatus()).isEqualTo(BookingStatus.PENDING);

            verify(accommodationRepository).findById(testAccommodation.getId());
        }

        @Test
        @DisplayName("Should throw exception when accommodation not found")
        void shouldThrowException_WhenAccommodationNotFound() {
            // Given
            when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testGuest);
            when(accommodationRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> bookingService.createBooking(testBookingRequest, httpServletRequest))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("Accommodation not found");
            verify(bookingRepository, never()).save(any(Booking.class));
        }

        @Test
        @DisplayName("Should throw exception when check-in is after check-out")
        void shouldThrowException_WhenCheckInIsAfterCheckOut() {
            // Given
            BookingRequest invalidRequest = new BookingRequest(
                    10, LocalDate.now().plusDays(10), LocalDate.now().plusDays(5), 2
            );
            when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testGuest);
            when(accommodationRepository.findById(testAccommodation.getId())).thenReturn(Optional.of(testAccommodation));

            // When & Then
            assertThatThrownBy(() -> bookingService.createBooking(invalidRequest, httpServletRequest))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("Check-in date must be before check-out date");
        }

        @Test
        @DisplayName("Should throw exception when dates are in the past")
        void shouldThrowException_WhenDatesAreInThePast() {
            // Given
            BookingRequest pastRequest = new BookingRequest(
                    10, LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), 2
            );
            when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testGuest);
            when(accommodationRepository.findById(testAccommodation.getId())).thenReturn(Optional.of(testAccommodation));

            // When & Then
            assertThatThrownBy(() -> bookingService.createBooking(pastRequest, httpServletRequest))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("Cannot book dates in the past");
        }

        @Test
        @DisplayName("Should throw exception when dates overlap (with details)")
        void shouldThrowException_WhenDatesOverlap() {
            // Given
            when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testGuest);
            when(accommodationRepository.findById(testAccommodation.getId())).thenReturn(Optional.of(testAccommodation));

            // 1. MOCK CORREGIDO: Simula que NO existe solapamiento simple (para evitar la Línea 69 del servicio)
            when(bookingRepository.existsOverlappingBooking(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(false);

            // 2. MOCK MANTENIDO: Simula que SÍ se encuentran reservas solapadas (para activar la Línea 72 del servicio)
            when(bookingRepository.findOverlappingBookings(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of(testBooking));


            // When & Then
            assertThatThrownBy(() -> bookingService.createBooking(testBookingRequest, httpServletRequest))
                    .isInstanceOf(ExceptionAlert.class)
                    // Asegúrate de que el mensaje a buscar coincida exactamente con el patrón que construye el servicio
                    .hasMessageContaining("Accommodation is not avaliable for the selected dates. Overlapping bookings:");

            verify(bookingRepository, never()).save(any(Booking.class));
        }

        //-------------------------------------------------------------------------
        // NESTED CLASS: GET BOOKINGS BY USER TESTS
        //-------------------------------------------------------------------------

        @Nested
        @DisplayName("Get Bookings By User")
        class GetBookingsByUserTests {
            private final PageRequest pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

            @Test
            @DisplayName("Should return all bookings for user when status is null")
            void shouldReturnAllBookings_WhenStatusIsNull() throws ExceptionAlert {
                // Given
                Page<Booking> bookingPage = new PageImpl<>(List.of(testBooking));
                when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testGuest);
                when(bookingRepository.findByGuestId(testGuest.getId(), pageable)).thenReturn(bookingPage);
                when(bookingMapper.toDto(testBooking)).thenReturn(testBookingDto);

                // When
                Page<BookingDto> result = bookingService.getBookingsByUser(null, 0, 10, httpServletRequest);

                // Then
                assertThat(result).hasSize(1);
                assertThat(result.getContent().get(0)).isEqualTo(testBookingDto);
                verify(bookingRepository).findByGuestId(testGuest.getId(), pageable);
                verify(bookingRepository, never()).findByGuestIdAndBookingStatus(anyLong(), any(BookingStatus.class), any(Pageable.class));
            }

            @Test
            @DisplayName("Should return filtered bookings when status is provided")
            void shouldReturnFilteredBookings_WhenStatusIsProvided() throws ExceptionAlert {
                // Given
                Page<Booking> bookingPage = new PageImpl<>(List.of(testBooking));
                when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testGuest);
                when(bookingRepository.findByGuestIdAndBookingStatus(testGuest.getId(), BookingStatus.PENDING, pageable)).thenReturn(bookingPage);
                when(bookingMapper.toDto(testBooking)).thenReturn(testBookingDto);

                // When
                Page<BookingDto> result = bookingService.getBookingsByUser("PENDING", 0, 10, httpServletRequest);

                // Then
                assertThat(result).hasSize(1);
                assertThat(result.getContent().get(0)).isEqualTo(testBookingDto);
                verify(bookingRepository).findByGuestIdAndBookingStatus(testGuest.getId(), BookingStatus.PENDING, pageable);
            }

            @Test
            @DisplayName("Should throw exception when user not found")
            void shouldThrowException_WhenUserNotFound() {
                // Given
                when(userService.getUserFromRequest(httpServletRequest)).thenReturn(null);

                // When & Then
                assertThatThrownBy(() -> bookingService.getBookingsByUser(null, 0, 10, httpServletRequest))
                        .isInstanceOf(ExceptionAlert.class)
                        .hasMessage("User not found");
            }
        }

        //-------------------------------------------------------------------------
        // NESTED CLASS: GET BOOKINGS BY HOST TESTS
        //-------------------------------------------------------------------------

        @Nested
        @DisplayName("Get Bookings By Host")
        class GetBookingsByHostTests {
            private final PageRequest pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

            @Test
            @DisplayName("Should return all host bookings when no filters are provided")
            void shouldReturnAllHostBookings_WhenNoFiltersProvided() throws ExceptionAlert {
                // Given
                Page<Booking> bookingPage = new PageImpl<>(List.of(testBooking));
                when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testHost);
                when(bookingRepository.findByAccommodationHostId(testHost.getId(), pageable)).thenReturn(bookingPage);
                when(bookingMapper.toDto(testBooking)).thenReturn(testBookingDto);

                // When
                Page<BookingDto> result = bookingService.getBookingsByHost(null, null, 0, 10, httpServletRequest);

                // Then
                assertThat(result).hasSize(1);
                verify(bookingRepository).findByAccommodationHostId(testHost.getId(), pageable);
            }

            @Test
            @DisplayName("Should return host bookings filtered by accommodation ID and Status")
            void shouldReturnHostBookings_FilteredByAccommodationIdAndStatus() throws ExceptionAlert {
                // Given
                Page<Booking> bookingPage = new PageImpl<>(List.of(testBooking));
                when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testHost);
                when(bookingRepository.findByAccommodationIdAndAccommodationHostIdAndBookingStatus(
                        testAccommodation.getId(), testHost.getId(), BookingStatus.PENDING, pageable
                )).thenReturn(bookingPage);
                when(bookingMapper.toDto(testBooking)).thenReturn(testBookingDto);

                // When
                Page<BookingDto> result = bookingService.getBookingsByHost(10, "PENDING", 0, 10, httpServletRequest);

                // Then
                assertThat(result).hasSize(1);
                verify(bookingRepository).findByAccommodationIdAndAccommodationHostIdAndBookingStatus(
                        testAccommodation.getId(), testHost.getId(), BookingStatus.PENDING, pageable
                );
            }

            @Test
            @DisplayName("Should throw exception when user is not a host")
            void shouldThrowException_WhenUserIsNotAHost() {
                // Given
                when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testGuest);

                // When & Then
                assertThatThrownBy(() -> bookingService.getBookingsByHost(null, null, 0, 10, httpServletRequest))
                        .isInstanceOf(ExceptionAlert.class)
                        .hasMessage("User is not a host");
                verify(bookingRepository, never()).findByAccommodationHostId(anyLong(), any(Pageable.class));
            }
        }

        //-------------------------------------------------------------------------
        // NESTED CLASS: CANCEL BOOKING TESTS (GUEST)
        //-------------------------------------------------------------------------

        @Nested
        @DisplayName("Cancel Booking (Guest)")
        class CancelBookingTests {

            @Test
            @DisplayName("Should cancel booking successfully when valid and timely")
            void shouldCancelBookingSuccessfully_WhenValidAndTimely() throws ExceptionAlert {
                // Given
                Long bookingId = 1L;
                Booking cancelableBooking = testBooking; // Start date is 5 days from now
                cancelableBooking.setBookingStatus(BookingStatus.CONFIRMED);

                when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testGuest);
                when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(cancelableBooking));

                // When
                bookingService.cancelBooking(bookingId, httpServletRequest);

                // Then
                ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
                verify(bookingRepository).save(bookingCaptor.capture());
                assertThat(bookingCaptor.getValue().getBookingStatus()).isEqualTo(BookingStatus.CANCELLED);
                assertThat(bookingCaptor.getValue().getCancelledAt()).isNotNull();

                verify(mailServiceImpl, times(2)).sendSimpleEmail(anyString(), anyString(), anyString()); // 1 to guest, 1 to host
            }

            @Test
            @DisplayName("Should throw exception when booking not found")
            void shouldThrowException_WhenBookingNotFound() {
                // Given
                when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testGuest);
                when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> bookingService.cancelBooking(999L, httpServletRequest))
                        .isInstanceOf(ExceptionAlert.class)
                        .hasMessage("Booking not found");
            }

            @Test
            @DisplayName("Should throw exception when already cancelled")
            void shouldThrowException_WhenAlreadyCancelled() {
                // Given
                testBooking.setBookingStatus(BookingStatus.CANCELLED);
                when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testGuest);
                when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));

                // When & Then
                assertThatThrownBy(() -> bookingService.cancelBooking(1L, httpServletRequest))
                        .isInstanceOf(ExceptionAlert.class)
                        .hasMessage("Booking has already been cancelled");
            }

            @Test
            @DisplayName("Should throw exception when cancellation is too late (less than 48h)")
            void shouldThrowException_WhenCancellationIsTooLate() {
                // Given
                testBooking.setStartDate(LocalDateTime.now().plusHours(24));
                testBooking.setBookingStatus(BookingStatus.CONFIRMED);
                when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testGuest);
                when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));

                // When & Then
                assertThatThrownBy(() -> bookingService.cancelBooking(1L, httpServletRequest))
                        .isInstanceOf(ExceptionAlert.class)
                        .hasMessage("Bookings can only be cancelled up to 48 hours before the start date");
                verify(bookingRepository, never()).save(any(Booking.class));
            }

            @Test
            @DisplayName("Should throw exception when user is not the booking owner")
            void shouldThrowException_WhenUserIsNotTheBookingOwner() {
                // Given
                User stranger = User.builder().id(99L).build();
                when(userService.getUserFromRequest(httpServletRequest)).thenReturn(stranger);
                when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));

                // When & Then
                assertThatThrownBy(() -> bookingService.cancelBooking(1L, httpServletRequest))
                        .isInstanceOf(ExceptionAlert.class)
                        .hasMessage("User is not the owner of the booking");
            }
        }

        //-------------------------------------------------------------------------
        // NESTED CLASS: COMPLETE BOOKING TESTS
        //-------------------------------------------------------------------------

        @Nested
        @DisplayName("Complete Booking")
        class CompleteBookingTests {

            @Test
            @DisplayName("Should complete booking successfully when valid")
            void shouldCompleteBookingSuccessfully_WhenValid() {
                // Given
                testBooking.setBookingStatus(BookingStatus.PENDING);
                when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testGuest);
                when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));

                // When
                InfoDto result = bookingService.completeBooking(1L, httpServletRequest);

                // Then
                assertThat(result.message()).isEqualTo("The booking has been marked as completed successfully");
                assertThat(result.title()).isEqualTo("Booking completed");

                ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
                verify(bookingRepository).save(bookingCaptor.capture());
                assertThat(bookingCaptor.getValue().getBookingStatus()).isEqualTo(BookingStatus.CONFIRMED);

                verify(mailServiceImpl, times(2)).sendSimpleEmail(anyString(), anyString(), anyString());
            }

            @Test
            @DisplayName("Should return info when already completed")
            void shouldReturnInfo_WhenAlreadyCompleted() {
                // Given
                testBooking.setBookingStatus(BookingStatus.COMPLETED);
                when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testGuest);
                when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));

                // When
                InfoDto result = bookingService.completeBooking(1L, httpServletRequest);

                // Then
                assertThat(result.title()).isEqualTo("Already completed");
                verify(bookingRepository, never()).save(any(Booking.class));
            }

            @Test
            @DisplayName("Should return info when cancelled")
            void shouldReturnInfo_WhenCancelled() {
                // Given
                testBooking.setBookingStatus(BookingStatus.CANCELLED);
                when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testGuest);
                when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));

                // When
                InfoDto result = bookingService.completeBooking(1L, httpServletRequest);

                // Then
                assertThat(result.title()).isEqualTo("Cannot complete");
                verify(bookingRepository, never()).save(any(Booking.class));
            }

            @Test
            @DisplayName("Should return info when user is not the booking owner")
            void shouldReturnInfo_WhenUserIsNotTheBookingOwner() {
                // Given
                User stranger = User.builder().id(99L).build();
                when(userService.getUserFromRequest(httpServletRequest)).thenReturn(stranger);
                when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));

                // When
                InfoDto result = bookingService.completeBooking(1L, httpServletRequest);

                // Then
                assertThat(result.title()).isEqualTo("Unauthorized");
                verify(bookingRepository, never()).save(any(Booking.class));
            }


            //-------------------------------------------------------------------------
            // NESTED CLASS: CANCEL BOOKING BY HOST TESTS
            //-------------------------------------------------------------------------

            @Nested
            @DisplayName("Cancel Booking By Host")
            class CancelBookingByHostTests {

                @Test
                @DisplayName("Should cancel booking successfully when host is the owner")
                void shouldCancelBookingSuccessfully_WhenHostIsOwner() {
                    // Given
                    testBooking.setBookingStatus(BookingStatus.PENDING);
                    when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testHost);
                    when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));

                    // When
                    bookingService.cancelBookingByHost(1L, httpServletRequest);

                    // Then
                    ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
                    verify(bookingRepository).save(bookingCaptor.capture());
                    assertThat(bookingCaptor.getValue().getBookingStatus()).isEqualTo(BookingStatus.CANCELLED);
                }

                @Test
                @DisplayName("Should log error when user is not a host")
                void shouldLogError_WhenUserIsNotAHost() {
                    // Given
                    when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testGuest);

                    // When
                    bookingService.cancelBookingByHost(1L, httpServletRequest);

                    // Then
                    // Solo verificamos que no se intenta guardar y que se llega al catch/log
                    verify(bookingRepository, never()).findById(anyLong());
                    verify(bookingRepository, never()).save(any(Booking.class));
                }

                @Test
                @DisplayName("Should log error when host is not the accommodation owner")
                void shouldLogError_WhenHostIsNotAccommodationOwner() {
                    // Given
                    User differentHost = User.builder().id(99L).role(UserRole.HOST).build();
                    when(userService.getUserFromRequest(httpServletRequest)).thenReturn(differentHost);
                    when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));

                    // When
                    bookingService.cancelBookingByHost(1L, httpServletRequest);

                    // Then
                    // Se espera que la excepción interna (lanzada con `new Exception`) sea capturada y logueada.
                    verify(bookingRepository).findById(anyLong());
                    verify(bookingRepository, never()).save(any(Booking.class));
                }
            }

            //-------------------------------------------------------------------------
            // NESTED CLASS: UTILITY AND STATUS CHECKS
            //-------------------------------------------------------------------------

            @Nested
            @DisplayName("Utility and Status Checks")
            class UtilityAndStatusChecksTests {

                @Test
                @DisplayName("Should return true when accommodation has future confirmed bookings")
                void shouldReturnTrue_WhenAccommodationHasFutureConfirmedBookings() {
                    // Given
                    Long accId = 10L;
                    when(accommodationRepository.existsById(accId)).thenReturn(true);
                    when(bookingRepository.existsByAccommodation_Id(accId)).thenReturn(true);
                    when(bookingRepository.existsByAccommodationIdAndEndDateAfterAndBookingStatusNot(
                            eq(accId), any(LocalDateTime.class), eq(BookingStatus.CONFIRMED)
                    )).thenReturn(true);

                    // When
                    boolean result = bookingService.accommodationHasFutureBookings(accId);

                    // Then
                    assertThat(result).isTrue();
                }

                @Test
                @DisplayName("Should return false when accommodation has no bookings")
                void shouldReturnFalse_WhenAccommodationHasNoBookings() {
                    // Given
                    Long accId = 10L;
                    when(accommodationRepository.existsById(accId)).thenReturn(true);
                    when(bookingRepository.existsByAccommodation_Id(accId)).thenReturn(false);

                    // When
                    boolean result = bookingService.accommodationHasFutureBookings(accId);

                    // Then
                    assertThat(result).isFalse();
                }

                @Test
                @DisplayName("Should return list of future confirmed booking dates")
                void shouldReturnListOfFutureConfirmedBookingDates() {
                    // Given
                    Long accId = 10L;
                    Booking futureBooking = testBooking;
                    futureBooking.setBookingStatus(BookingStatus.CONFIRMED);
                    futureBooking.setStartDate(LocalDate.now().plusDays(2).atStartOfDay());
                    futureBooking.setEndDate(LocalDate.now().plusDays(4).atStartOfDay());

                    BookingDatesDto expectedDto = new BookingDatesDto(
                            futureBooking.getStartDate().toLocalDate(),
                            futureBooking.getEndDate().toLocalDate()
                    );

                    when(bookingRepository.findFutureConfirmedBookingsByAccommodation(eq(accId), any(LocalDateTime.class)))
                            .thenReturn(List.of(futureBooking));

                    // When
                    List<BookingDatesDto> result = bookingService.getFutureConfirmedBookingDates(accId);

                    // Then
                    assertThat(result).hasSize(1);
                    assertThat(result.get(0)).isEqualTo(expectedDto);
                }

                @Test
                @DisplayName("Should return booking by ID")
                void shouldReturnBookingById() throws ExceptionAlert {
                    // Given
                    Long bookingId = 1L;
                    when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(testBooking));

                    // When
                    Booking result = bookingService.getBookingById(bookingId);

                    // Then
                    assertThat(result).isEqualTo(testBooking);
                }

                @Test
                @DisplayName("Should throw exception when getBookingById not found")
                void shouldThrowException_WhenGetBookingByIdNotFound() {
                    // Given
                    when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

                    // When & Then
                    assertThatThrownBy(() -> bookingService.getBookingById(999L))
                            .isInstanceOf(ExceptionAlert.class)
                            .hasMessage("Booking not found");
                }
            }
        }
    }
}