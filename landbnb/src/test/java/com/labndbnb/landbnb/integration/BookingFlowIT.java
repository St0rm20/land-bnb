package com.labndbnb.landbnb.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Integración: reservas")
class BookingFlowIT extends IntegrationTestSupport {

    @Test
    @DisplayName("el total se calcula exacto con decimales y la reserva nace PENDING")
    void exactPriceAndPendingStatus() throws Exception {
        String host = newHost();
        String guest = newUser();
        long acc = createAccommodation(host, "99.99");

        doPost("/api/booking", guest, bookingBody(acc, 10, 13, 2))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalPrice").value(299.97))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.numberOfGuests").value(2));
    }

    @Test
    @DisplayName("solo el host dueño confirma: el huésped y otro host reciben error")
    void onlyOwnerHostConfirms() throws Exception {
        String host = newHost();
        String otherHost = newHost();
        String guest = newUser();
        long booking = createBooking(guest, createAccommodation(host, "100000.00"), 10, 12);

        doPost("/api/booking/" + booking + "/confirm", guest, null).andExpect(status().isForbidden());
        doPost("/api/booking/" + booking + "/confirm", otherHost, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.content").value("User is not the host of the accommodation for this booking"));

        doPost("/api/booking/" + booking + "/confirm", host, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Booking confirmed"));
        doGet("/api/booking/" + booking, guest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("no se puede reservar un rango solapado con otra reserva")
    void overlappingBookingRejected() throws Exception {
        String host = newHost();
        long acc = createAccommodation(host, "100000.00");
        createBooking(newUser(), acc, 10, 14);

        doPost("/api/booking", newUser(), bookingBody(acc, 12, 16, 2))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.content").value("Accommodation is not available for the selected dates"));
        // fechas contiguas (el check-out del uno es el check-in del otro) sí están permitidas
        doPost("/api/booking", newUser(), bookingBody(acc, 14, 16, 2)).andExpect(status().isCreated());
    }

    @Test
    @DisplayName("validaciones: 0 noches, capacidad excedida, host reservando lo suyo")
    void businessValidations() throws Exception {
        String host = newHost();
        String guest = newUser();
        long acc = createAccommodation(host, "100000.00");

        doPost("/api/booking", guest, bookingBody(acc, 10, 10, 2)).andExpect(status().isBadRequest());
        doPost("/api/booking", guest, bookingBody(acc, 10, 12, 5))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.content").value(org.hamcrest.Matchers.containsString("capacity")));
        doPost("/api/booking", host, bookingBody(acc, 10, 12, 2))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.content").value("Hosts cannot book their own accommodations"));
    }

    @Test
    @DisplayName("cancelar libera las fechas para otro huésped")
    void cancelFreesTheDates() throws Exception {
        String host = newHost();
        String guest = newUser();
        long acc = createAccommodation(host, "100000.00");
        long booking = createBooking(guest, acc, 10, 12);

        doPost("/api/booking/" + booking + "/cancel", guest, null).andExpect(status().isOk());

        doGet("/api/booking/" + booking, guest).andExpect(jsonPath("$.status").value("CANCELLED"));
        doPost("/api/booking", newUser(), bookingBody(acc, 10, 12, 2)).andExpect(status().isCreated());
    }

    @Test
    @DisplayName("un extraño no puede ver ni cancelar una reserva ajena")
    void strangerCannotAccessBooking() throws Exception {
        long booking = createBooking(newUser(), createAccommodation(newHost(), "100000.00"), 10, 12);
        String stranger = newUser();

        doGet("/api/booking/" + booking, stranger).andExpect(status().isBadRequest());
        doPost("/api/booking/" + booking + "/cancel", stranger, null).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("las fechas confirmadas aparecen como no disponibles (endpoint público)")
    void confirmedDatesAreUnavailable() throws Exception {
        String host = newHost();
        long acc = createAccommodation(host, "100000.00");
        long booking = createBooking(newUser(), acc, 20, 23);
        doPost("/api/booking/" + booking + "/confirm", host, null).andExpect(status().isOk());

        doGet("/api/accommodations/dates-unavailable/" + acc, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].checkInDate").value(LocalDate.now().plusDays(20).toString()))
                .andExpect(jsonPath("$[0].checkOutDate").value(LocalDate.now().plusDays(23).toString()));
    }

    @Test
    @DisplayName("un estado de filtro inválido da 400, no 500")
    void invalidStatusFilterIs400() throws Exception {
        String guest = newUser();

        mvc.perform(withToken(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/booking/user").param("status", "NOPE"), guest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("el host ve las reservas de sus alojamientos filtradas por estado")
    void hostListsBookings() throws Exception {
        String host = newHost();
        long acc = createAccommodation(host, "100000.00");
        long b1 = createBooking(newUser(), acc, 10, 12);
        createBooking(newUser(), acc, 20, 22);
        doPost("/api/booking/" + b1 + "/confirm", host, null).andExpect(status().isOk());

        mvc.perform(withToken(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/booking/host").param("status", "CONFIRMED"), host))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(b1));
    }

    /**
     * Concurrencia real: varios huéspedes piden el mismo rango a la vez.
     * El bloqueo de fila del alojamiento debe dejar pasar exactamente una reserva.
     */
    @Test
    @DisplayName("reservas simultáneas del mismo rango: solo una se crea")
    void concurrentBookingsOnlyOneSucceeds() throws Exception {
        String host = newHost();
        long acc = createAccommodation(host, "100000.00");
        int threads = 6;
        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < threads; i++) tokens.add(newUser());

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Integer>> results = new ArrayList<>();
        for (String token : tokens) {
            results.add(pool.submit(() -> {
                start.await();
                return doPost("/api/booking", token, bookingBody(acc, 30, 33, 2))
                        .andReturn().getResponse().getStatus();
            }));
        }
        start.countDown();
        List<Integer> statuses = new ArrayList<>();
        for (Future<Integer> f : results) statuses.add(f.get(60, TimeUnit.SECONDS));
        pool.shutdown();

        assertThat(statuses).filteredOn(s -> s == 201).hasSize(1);
        assertThat(statuses).filteredOn(s -> s == 400).hasSize(threads - 1);
    }
}
