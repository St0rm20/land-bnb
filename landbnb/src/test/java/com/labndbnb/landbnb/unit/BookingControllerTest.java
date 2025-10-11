package com.labndbnb.landbnb.unit;

import com.labndbnb.landbnb.controller.BookingController;
import com.labndbnb.landbnb.dto.booking_dto.BookingDto;
import com.labndbnb.landbnb.dto.booking_dto.BookingRequest;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.service.definition.BookingService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para BookingController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Booking Controller Unit Tests")
class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    @Mock
    private HttpServletRequest request;

    private BookingRequest bookingRequest;
    private BookingDto bookingDto;
    private InfoDto infoDto;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        // BookingRequest
        bookingRequest = new BookingRequest(
                1,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                2
        );

        // BookingDto
        bookingDto = new BookingDto(
                1,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                2,
                240000.0,
                "PENDING",
                null, // accommodation
                null  // user
        );

        // InfoDto
        infoDto = new InfoDto("Success", "Operation completed successfully");
    }

    @Nested
    @DisplayName("POST /api/booking - Crear reserva")
    class CreateBookingTests {

        @Test
        @DisplayName("Debería crear reserva exitosamente")
        void givenValidBookingRequest_whenCreateBooking_thenReturnCreated() {
            // Given
            when(bookingService.createBooking(any(BookingRequest.class), any(HttpServletRequest.class)))
                    .thenReturn(bookingDto);

            // When
            ResponseEntity<?> response = bookingController.createBooking(bookingRequest, request);

            // Then
            assertAll(
                    () -> assertEquals(HttpStatus.CREATED, response.getStatusCode()),
                    () -> assertNotNull(response.getBody()),
                    () -> assertEquals(bookingDto, response.getBody()),
                    () -> verify(bookingService, times(1))
                            .createBooking(any(BookingRequest.class), any(HttpServletRequest.class))
            );
        }

        @Test
        @DisplayName("Debería retornar BAD_REQUEST cuando la creación falla")
        void givenInvalidBookingRequest_whenCreateBooking_thenReturnBadRequest() {
            // Given
            when(bookingService.createBooking(any(BookingRequest.class), any(HttpServletRequest.class)))
                    .thenThrow(new ExceptionAlert("Accommodation not found"));

            // When
            ResponseEntity<?> response = bookingController.createBooking(bookingRequest, request);

            // Then
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                    () -> assertNotNull(response.getBody()),
                    () -> assertTrue(response.getBody() instanceof InfoDto)
            );
        }
    }

    @Nested
    @DisplayName("GET /api/booking/user - Obtener reservas del usuario")
    class GetBookingsUserTests {

        @Test
        @DisplayName("Debería retornar reservas del usuario exitosamente")
        void givenAuthenticatedUser_whenGetBookingsUser_thenReturnBookings() throws ExceptionAlert {
            // Given
            Page<BookingDto> bookingsPage = new PageImpl<>(Arrays.asList(bookingDto));
            when(bookingService.getBookingsByUser(anyString(), anyInt(), anyInt(), any(HttpServletRequest.class)))
                    .thenReturn(bookingsPage);

            // When
            ResponseEntity<?> response = bookingController.getBookingsUser("PENDING", 0, 10, request);

            // Then
            assertAll(
                    () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                    () -> assertNotNull(response.getBody()),
                    () -> verify(bookingService, times(1))
                            .getBookingsByUser(anyString(), anyInt(), anyInt(), any(HttpServletRequest.class))
            );
        }

        @Test
        @DisplayName("Debería retornar BAD_REQUEST cuando falla la obtención de reservas")
        void givenInvalidRequest_whenGetBookingsUser_thenReturnBadRequest() throws ExceptionAlert {
            // Given
            when(bookingService.getBookingsByUser(anyString(), anyInt(), anyInt(), any(HttpServletRequest.class)))
                    .thenThrow(new ExceptionAlert("User not found"));

            // When
            ResponseEntity<?> response = bookingController.getBookingsUser("PENDING", 0, 10, request);

            // Then
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("Debería retornar todas las reservas cuando no se especifica estado")
        void givenNoStatus_whenGetBookingsUser_thenReturnAllBookings() throws ExceptionAlert {
            // Given
            Page<BookingDto> bookingsPage = new PageImpl<>(Arrays.asList(bookingDto));
            when(bookingService.getBookingsByUser(isNull(), anyInt(), anyInt(), any(HttpServletRequest.class)))
                    .thenReturn(bookingsPage);

            // When
            ResponseEntity<?> response = bookingController.getBookingsUser(null, 0, 10, request);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("GET /api/booking/host - Obtener reservas del host")
    class GetBookingsHostTests {

        @Test
        @DisplayName("Debería retornar reservas del host exitosamente")
        void givenAuthenticatedHost_whenGetBookingsHost_thenReturnBookings() throws ExceptionAlert {
            // Given
            Page<BookingDto> bookingsPage = new PageImpl<>(Arrays.asList(bookingDto));
            when(bookingService.getBookingsByHost(anyInt(), anyString(), anyInt(), anyInt(), any(HttpServletRequest.class)))
                    .thenReturn(bookingsPage);

            // When
            ResponseEntity<?> response = bookingController.getBookingsHost(1, "PENDING", 0, 10, request);

            // Then
            assertAll(
                    () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                    () -> assertNotNull(response.getBody()),
                    () -> verify(bookingService, times(1))
                            .getBookingsByHost(anyInt(), anyString(), anyInt(), anyInt(), any(HttpServletRequest.class))
            );
        }

        @Test
        @DisplayName("Debería retornar BAD_REQUEST cuando falla la obtención de reservas del host")
        void givenInvalidRequest_whenGetBookingsHost_thenReturnBadRequest() throws ExceptionAlert {
            // Given
            when(bookingService.getBookingsByHost(anyInt(), anyString(), anyInt(), anyInt(), any(HttpServletRequest.class)))
                    .thenThrow(new ExceptionAlert("User is not a host"));

            // When
            ResponseEntity<?> response = bookingController.getBookingsHost(1, "PENDING", 0, 10, request);

            // Then
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("Debería retornar todas las reservas del host sin filtros")
        void givenNoFilters_whenGetBookingsHost_thenReturnAllHostBookings() throws ExceptionAlert {
            // Given
            Page<BookingDto> bookingsPage = new PageImpl<>(Arrays.asList(bookingDto));
            when(bookingService.getBookingsByHost(isNull(), isNull(), anyInt(), anyInt(), any(HttpServletRequest.class)))
                    .thenReturn(bookingsPage);

            // When
            ResponseEntity<?> response = bookingController.getBookingsHost(null, null, 0, 10, request);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("POST /api/booking/{id}/cancel - Cancelar reserva")
    class CancelBookingTests {

        @Test
        @DisplayName("Debería cancelar reserva exitosamente")
        void givenValidBookingId_whenCancelBooking_thenReturnSuccess() throws ExceptionAlert {
            // Given
            doNothing().when(bookingService).cancelBooking(anyLong(), any(HttpServletRequest.class));

            // When
            ResponseEntity<?> response = bookingController.cancelBooking(1L, request);

            // Then
            assertAll(
                    () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                    () -> assertNotNull(response.getBody()),
                    () -> verify(bookingService, times(1))
                            .cancelBooking(anyLong(), any(HttpServletRequest.class))
            );
        }

        @Test
        @DisplayName("Debería retornar BAD_REQUEST cuando la cancelación falla")
        void givenInvalidBookingId_whenCancelBooking_thenReturnBadRequest() throws ExceptionAlert {
            // Given
            doThrow(new ExceptionAlert("Booking not found"))
                    .when(bookingService).cancelBooking(anyLong(), any(HttpServletRequest.class));

            // When
            ResponseEntity<?> response = bookingController.cancelBooking(1L, request);

            // Then
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("POST /api/booking/host/{id}/cancel - Cancelar reserva por host")
    class CancelBookingByHostTests {

        @Test
        @DisplayName("Debería cancelar reserva por host exitosamente")
        void givenValidBookingId_whenCancelBookingByHost_thenReturnSuccess() {
            // Given
            doNothing().when(bookingService).cancelBookingByHost(anyLong(), any(HttpServletRequest.class));

            // When
            ResponseEntity<?> response = bookingController.cancelBookingByHost(1L, request);

            // Then
            assertAll(
                    () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                    () -> assertNotNull(response.getBody()),
                    () -> verify(bookingService, times(1))
                            .cancelBookingByHost(anyLong(), any(HttpServletRequest.class))
            );
        }

        @Test
        @DisplayName("Debería retornar BAD_REQUEST cuando la cancelación por host falla")
        void givenInvalidBookingId_whenCancelBookingByHost_thenReturnBadRequest() {
            // Given
            // El servicio maneja la excepción internamente y el controller siempre retorna OK/BAD_REQUEST
            doThrow(new RuntimeException("Booking not found"))
                    .when(bookingService).cancelBookingByHost(anyLong(), any(HttpServletRequest.class));

            // When
            ResponseEntity<?> response = bookingController.cancelBookingByHost(1L, request);

            // Then
            // El controller captura la excepción y retorna BAD_REQUEST
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("POST /api/booking/{id}/confirm - Confirmar reserva")
    class ConfirmBookingTests {

        @Test
        @DisplayName("Debería confirmar reserva exitosamente")
        void givenValidBookingId_whenConfirmBooking_thenReturnSuccess() {
            // Given
            doNothing().when(bookingService).completeBooking(anyLong(), any(HttpServletRequest.class));

            // When
            ResponseEntity<?> response = bookingController.confirmBooking(1L, request);

            // Then
            assertAll(
                    () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                    () -> assertNotNull(response.getBody()),
                    () -> verify(bookingService, times(1))
                            .completeBooking(anyLong(), any(HttpServletRequest.class))
            );
        }

        @Test
        @DisplayName("Debería retornar BAD_REQUEST cuando la confirmación falla")
        void givenInvalidBookingId_whenConfirmBooking_thenReturnBadRequest() {
            // Given
            // El servicio maneja la excepción internamente y el controller siempre retorna OK/BAD_REQUEST
            doThrow(new RuntimeException("Booking not found"))
                    .when(bookingService).completeBooking(anyLong(), any(HttpServletRequest.class));

            // When
            ResponseEntity<?> response = bookingController.confirmBooking(1L, request);

            // Then
            // El controller captura la excepción y retorna BAD_REQUEST
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("Casos de búsqueda específicos")
    class SpecificSearchCases {

        @Test
        @DisplayName("Debería manejar búsqueda de reservas del usuario sin estado")
        void givenUserWithoutStatus_whenGetBookingsUser_thenReturnAllUserBookings() throws ExceptionAlert {
            // Given
            Page<BookingDto> bookingsPage = new PageImpl<>(Arrays.asList(bookingDto));
            when(bookingService.getBookingsByUser(isNull(), anyInt(), anyInt(), any(HttpServletRequest.class)))
                    .thenReturn(bookingsPage);

            // When
            ResponseEntity<?> response = bookingController.getBookingsUser(null, 0, 10, request);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        @DisplayName("Debería manejar búsqueda de reservas del host sin accommodationId")
        void givenHostWithoutAccommodationId_whenGetBookingsHost_thenReturnAllHostBookings() throws ExceptionAlert {
            // Given
            Page<BookingDto> bookingsPage = new PageImpl<>(Arrays.asList(bookingDto));
            when(bookingService.getBookingsByHost(isNull(), anyString(), anyInt(), anyInt(), any(HttpServletRequest.class)))
                    .thenReturn(bookingsPage);

            // When
            ResponseEntity<?> response = bookingController.getBookingsHost(null, "CONFIRMED", 0, 10, request);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        @DisplayName("Debería manejar búsqueda de reservas del host sin estado")
        void givenHostWithoutStatus_whenGetBookingsHost_thenReturnAllHostBookings() throws ExceptionAlert {
            // Given
            Page<BookingDto> bookingsPage = new PageImpl<>(Arrays.asList(bookingDto));
            when(bookingService.getBookingsByHost(anyInt(), isNull(), anyInt(), anyInt(), any(HttpServletRequest.class)))
                    .thenReturn(bookingsPage);

            // When
            ResponseEntity<?> response = bookingController.getBookingsHost(1, null, 0, 10, request);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }
}