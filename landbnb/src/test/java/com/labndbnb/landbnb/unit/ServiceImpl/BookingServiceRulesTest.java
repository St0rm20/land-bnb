package com.labndbnb.landbnb.unit.ServiceImpl;

import com.labndbnb.landbnb.dto.booking_dto.BookingRequest;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Reglas de negocio de reservas añadidas en la revisión (capacidad, fechas, bloqueo, estados). */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookingServiceImpl - reglas de negocio")
class BookingServiceRulesTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private AccommodationRepository accommodationRepository;
    @Mock private UserService userService;
    @Mock private BookingMapper bookingMapper;
    @Mock private MailServiceImpl mailService;

    @InjectMocks private BookingServiceImpl service;

    private User host;
    private User guest;
    private Accommodation accommodation;

    @BeforeEach
    void setUp() {
        host = User.builder().id(1L).email("h@x.com").role(UserRole.HOST).name("H").lastName("H").build();
        guest = User.builder().id(2L).email("g@x.com").role(UserRole.USER).name("G").lastName("G").build();
        accommodation = Accommodation.builder().id(10L).name("Casa").pricePerNight(new java.math.BigDecimal("100.00"))
                .capacity(4).host(host).build();
    }

    private BookingRequest req(int inDays, int outDays, int guests) {
        return new BookingRequest(10, LocalDate.now().plusDays(inDays), LocalDate.now().plusDays(outDays), guests);
    }

    private void stubLockedAccommodation() throws ExceptionAlert {
        when(userService.getActiveUser(2L)).thenReturn(guest);
        when(accommodationRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(accommodation));
    }

    @Nested
    @DisplayName("createBooking")
    class Create {

        @Test
        @DisplayName("lee el alojamiento con bloqueo de fila (evita reservas simultáneas solapadas)")
        void usesPessimisticLock() throws ExceptionAlert {
            stubLockedAccommodation();
            when(bookingRepository.findOverlappingBookings(anyLong(), any(), any())).thenReturn(List.of());
            when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

            service.createBooking(req(1, 3, 2), 2L);

            verify(accommodationRepository).findByIdForUpdate(10L);
            verify(accommodationRepository, never()).findById(anyLong());
        }

        @Test
        @DisplayName("calcula precio por noches y deja la reserva en PENDING")
        void priceAndStatus() throws ExceptionAlert {
            stubLockedAccommodation();
            when(bookingRepository.findOverlappingBookings(anyLong(), any(), any())).thenReturn(List.of());
            when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

            service.createBooking(req(1, 4, 2), 2L);

            ArgumentCaptor<Booking> saved = ArgumentCaptor.forClass(Booking.class);
            verify(bookingRepository).save(saved.capture());
            assertThat(saved.getValue().getTotalPrice()).isEqualByComparingTo(new java.math.BigDecimal("300"));
            assertThat(saved.getValue().getBookingStatus()).isEqualTo(BookingStatus.PENDING);
            assertThat(saved.getValue().getBookingCode()).hasSize(12);
        }

        @Test
        @DisplayName("el total es exacto con decimales (3 x 99.99 = 299.97, sin error de punto flotante)")
        void totalPriceIsExactDecimal() throws ExceptionAlert {
            accommodation.setPricePerNight(new java.math.BigDecimal("99.99"));
            stubLockedAccommodation();
            when(bookingRepository.findOverlappingBookings(anyLong(), any(), any())).thenReturn(List.of());
            when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

            service.createBooking(req(1, 4, 2), 2L);

            ArgumentCaptor<Booking> saved = ArgumentCaptor.forClass(Booking.class);
            verify(bookingRepository).save(saved.capture());
            assertThat(saved.getValue().getTotalPrice()).isEqualTo(new java.math.BigDecimal("299.97"));
        }

        @Test
        @DisplayName("rechaza check-in igual a check-out (0 noches)")
        void rejectsZeroNights() throws ExceptionAlert {
            stubLockedAccommodation();

            assertThatThrownBy(() -> service.createBooking(req(2, 2, 2), 2L))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("Check-in date must be before check-out date");
            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("rechaza más huéspedes que la capacidad")
        void rejectsOverCapacity() throws ExceptionAlert {
            stubLockedAccommodation();

            assertThatThrownBy(() -> service.createBooking(req(1, 3, 5), 2L))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessageContaining("capacity");
            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("rechaza alojamientos inactivos")
        void rejectsInactive() throws ExceptionAlert {
            accommodation.setActive(false);
            stubLockedAccommodation();

            assertThatThrownBy(() -> service.createBooking(req(1, 3, 2), 2L))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("Accommodation is not available");
        }

        @Test
        @DisplayName("el host no puede reservar su propio alojamiento")
        void hostCannotBookOwn() throws ExceptionAlert {
            when(userService.getActiveUser(1L)).thenReturn(host);
            when(accommodationRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(accommodation));

            assertThatThrownBy(() -> service.createBooking(req(1, 3, 2), 1L))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("Hosts cannot book their own accommodations");
        }

        @Test
        @DisplayName("el mensaje de solape NO expone datos de reservas ajenas")
        void overlapMessageDoesNotLeak() throws ExceptionAlert {
            stubLockedAccommodation();
            Booking other = Booking.builder().id(99L)
                    .startDate(LocalDate.now().plusDays(1).atStartOfDay())
                    .endDate(LocalDate.now().plusDays(3).atStartOfDay())
                    .bookingStatus(BookingStatus.CONFIRMED).build();
            when(bookingRepository.findOverlappingBookings(anyLong(), any(), any())).thenReturn(List.of(other));

            assertThatThrownBy(() -> service.createBooking(req(1, 3, 2), 2L))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("Accommodation is not available for the selected dates")
                    .message().doesNotContain("CONFIRMED").doesNotContain(other.getStartDate().toLocalDate().toString());
        }
    }

    @Nested
    @DisplayName("getBookingsByUser")
    class Listing {

        @Test
        @DisplayName("un estado inválido da ExceptionAlert (400) y no IllegalArgumentException (500)")
        void invalidStatus() throws ExceptionAlert {
            when(userService.getActiveUser(2L)).thenReturn(guest);

            assertThatThrownBy(() -> service.getBookingsByUser("NOPE", 0, 10, 2L))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessageContaining("Invalid booking status");
        }
    }

    @Nested
    @DisplayName("confirmBooking (solo el host)")
    class Confirm {

        private Booking pending() {
            return Booking.builder().id(5L).guest(guest).accommodation(accommodation)
                    .startDate(LocalDateTime.now().plusDays(5)).endDate(LocalDateTime.now().plusDays(7))
                    .bookingCode("ABC123456789").bookingStatus(BookingStatus.PENDING).build();
        }

        @Test
        @DisplayName("el host del alojamiento confirma una reserva PENDING y notifica después de guardar")
        void hostConfirmsPending() throws ExceptionAlert {
            Booking b = pending();
            when(userService.getActiveUser(1L)).thenReturn(host);
            when(bookingRepository.findById(5L)).thenReturn(Optional.of(b));
            when(bookingRepository.findOverlappingBookings(anyLong(), any(), any())).thenReturn(List.of(b));

            var info = service.confirmBooking(5L, 1L);

            assertThat(info.title()).isEqualTo("Booking confirmed");
            assertThat(b.getBookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
            var order = inOrder(bookingRepository, mailService);
            order.verify(bookingRepository).save(b);
            order.verify(mailService, times(2)).sendSimpleEmail(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("el huésped ya NO puede confirmar su propia reserva")
        void guestCannotSelfConfirm() throws ExceptionAlert {
            when(userService.getActiveUser(2L)).thenReturn(guest);

            assertThatThrownBy(() -> service.confirmBooking(5L, 2L))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("User is not a host");
            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("un host que no es dueño del alojamiento no puede confirmar")
        void otherHostCannotConfirm() throws ExceptionAlert {
            User otherHost = User.builder().id(77L).role(UserRole.HOST).build();
            when(userService.getActiveUser(77L)).thenReturn(otherHost);
            when(bookingRepository.findById(5L)).thenReturn(Optional.of(pending()));

            assertThatThrownBy(() -> service.confirmBooking(5L, 77L))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("User is not the host of the accommodation for this booking");
            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("falla si otra reserva CONFIRMADA ocupa esas fechas")
        void failsOnConfirmedConflict() throws ExceptionAlert {
            Booking b = pending();
            Booking other = Booking.builder().id(6L).bookingStatus(BookingStatus.CONFIRMED).build();
            when(userService.getActiveUser(1L)).thenReturn(host);
            when(bookingRepository.findById(5L)).thenReturn(Optional.of(b));
            when(bookingRepository.findOverlappingBookings(anyLong(), any(), any())).thenReturn(List.of(b, other));

            assertThatThrownBy(() -> service.confirmBooking(5L, 1L))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("Accommodation is not available for the selected dates");
            assertThat(b.getBookingStatus()).isEqualTo(BookingStatus.PENDING);
            verify(mailService, never()).sendSimpleEmail(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("cancelBooking / cancelBookingByHost")
    class Cancel {

        @Test
        @DisplayName("el huésped cancela: se guarda antes de enviar los correos")
        void guestCancelPersistsBeforeMail() throws ExceptionAlert {
            Booking b = Booking.builder().id(5L).guest(guest).accommodation(accommodation)
                    .startDate(LocalDateTime.now().plusDays(10)).endDate(LocalDateTime.now().plusDays(12))
                    .bookingCode("ABC123456789").bookingStatus(BookingStatus.PENDING).build();
            when(userService.getActiveUser(2L)).thenReturn(guest);
            when(bookingRepository.findById(5L)).thenReturn(Optional.of(b));

            service.cancelBooking(5L, 2L);

            assertThat(b.getBookingStatus()).isEqualTo(BookingStatus.CANCELLED);
            assertThat(b.getCancelledAt()).isNotNull();
            var order = inOrder(bookingRepository, mailService);
            order.verify(bookingRepository).save(b);
            order.verify(mailService, atLeastOnce()).sendSimpleEmail(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("el host que cancela ve el motivo real del error, no un mensaje genérico")
        void hostCancelKeepsRealMessage() throws ExceptionAlert {
            User otherHost = User.builder().id(77L).role(UserRole.HOST).build();
            Booking b = Booking.builder().id(5L).guest(guest).accommodation(accommodation)
                    .bookingStatus(BookingStatus.PENDING).build();
            when(userService.getActiveUser(77L)).thenReturn(otherHost);
            when(bookingRepository.findById(5L)).thenReturn(Optional.of(b));

            assertThatThrownBy(() -> service.cancelBookingByHost(5L, 77L))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("User is not the host of the accommodation for this booking");
            verify(bookingRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("accommodationHasFutureBookings")
    class FutureBookings {

        @Test
        @DisplayName("solo cuentan reservas PENDING y CONFIRMED (las canceladas no bloquean el borrado)")
        void onlyActiveStatusesBlock() {
            when(accommodationRepository.existsById(10L)).thenReturn(true);
            when(bookingRepository.existsByAccommodation_Id(10L)).thenReturn(true);
            when(bookingRepository.existsByAccommodationIdAndEndDateAfterAndBookingStatusIn(
                    eq(10L), any(LocalDateTime.class), any())).thenReturn(false);

            assertThat(service.accommodationHasFutureBookings(10L)).isFalse();

            @SuppressWarnings("unchecked")
            ArgumentCaptor<Set<BookingStatus>> statuses = ArgumentCaptor.forClass(Set.class);
            verify(bookingRepository).existsByAccommodationIdAndEndDateAfterAndBookingStatusIn(
                    eq(10L), any(LocalDateTime.class), statuses.capture());
            assertThat(statuses.getValue()).containsExactlyInAnyOrder(BookingStatus.PENDING, BookingStatus.CONFIRMED);
        }
    }
}
