package com.labndbnb.landbnb.integration;

import com.labndbnb.landbnb.model.Booking;
import com.labndbnb.landbnb.model.enums.BookingStatus;
import com.labndbnb.landbnb.repository.BookingRepository;
import com.labndbnb.landbnb.service.definition.BookingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AutoCompleteBookingsIT extends IntegrationTestSupport {

    @Autowired BookingService bookingService;
    @Autowired BookingRepository bookingRepository;

    private void moveToPast(long bookingId) {
        Booking b = bookingRepository.findById(bookingId).orElseThrow();
        b.setStartDate(LocalDateTime.now().minusDays(5));
        b.setEndDate(LocalDateTime.now().minusDays(2));
        bookingRepository.save(b);
    }

    @Test
    @DisplayName("solo las reservas CONFIRMED ya terminadas pasan a COMPLETED")
    void completesOnlyFinishedConfirmedBookings() throws Exception {
        String host = newHost();
        long acc = createAccommodation(host, "100000.00");

        long finished = createBooking(newUser(), acc, 10, 12);
        doPost("/api/booking/" + finished + "/confirm", host, null).andExpect(status().isOk());
        moveToPast(finished);

        long upcoming = createBooking(newUser(), acc, 30, 33);
        doPost("/api/booking/" + upcoming + "/confirm", host, null).andExpect(status().isOk());

        long pendingPast = createBooking(newUser(), acc, 40, 42); // nunca confirmada
        moveToPast(pendingPast);

        bookingService.autoCompletePastBookings();

        Booking done = bookingRepository.findById(finished).orElseThrow();
        assertThat(done.getBookingStatus()).isEqualTo(BookingStatus.COMPLETED);
        assertThat(done.getCompletedAt()).isNotNull();
        assertThat(bookingRepository.findById(upcoming).orElseThrow().getBookingStatus())
                .isEqualTo(BookingStatus.CONFIRMED);
        assertThat(bookingRepository.findById(pendingPast).orElseThrow().getBookingStatus())
                .isEqualTo(BookingStatus.PENDING);

        // idempotente: una segunda ejecución no cambia nada
        LocalDateTime firstCompletion = done.getCompletedAt();
        bookingService.autoCompletePastBookings();
        assertThat(bookingRepository.findById(finished).orElseThrow().getCompletedAt()).isEqualTo(firstCompletion);
    }

    @Test
    @DisplayName("una reserva completada permite reseñar al huésped")
    void completedBookingCanBeReviewed() throws Exception {
        String host = newHost();
        String guest = newUser();
        long acc = createAccommodation(host, "100000.00");
        long booking = createBooking(guest, acc, 10, 12);
        doPost("/api/booking/" + booking + "/confirm", host, null).andExpect(status().isOk());
        moveToPast(booking);
        bookingService.autoCompletePastBookings();

        doGet("/api/comments/can-comment/" + acc, guest).andExpect(status().isOk());
    }
}
