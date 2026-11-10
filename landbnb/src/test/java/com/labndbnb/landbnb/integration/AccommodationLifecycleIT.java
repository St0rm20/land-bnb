package com.labndbnb.landbnb.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Integración: alojamientos")
class AccommodationLifecycleIT extends IntegrationTestSupport {

    private long createInCity(String host, String city, String price) throws Exception {
        Map<String, Object> body = accommodationBody(price);
        body.put("city", city);
        var r = doPost("/api/accommodations", host, body).andExpect(status().isCreated());
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("ID (\\d+)")
                .matcher(body(r).get("message").asText());
        m.find();
        return Long.parseLong(m.group(1));
    }

    private Map<String, Object> search(String city, String sortBy, String dir) {
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("city", city);
        if (sortBy != null) c.put("sortBy", sortBy);
        if (dir != null) c.put("sortDirection", dir);
        return c;
    }

    @Test
    @DisplayName("buscar ordenando por precio funciona y respeta el orden (antes daba 500)")
    void searchSortedByPrice() throws Exception {
        String host = newHost();
        String city = "Ciudad-" + UUID.randomUUID();
        createInCity(host, city, "300000.00");
        createInCity(host, city, "100000.00");
        createInCity(host, city, "200000.00");

        doPost("/api/accommodations/search", null, search(city, "price", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].pricePerNight").value(100000.00))
                .andExpect(jsonPath("$.content[2].pricePerNight").value(300000.00));
        doPost("/api/accommodations/search", null, search(city, "price", "desc"))
                .andExpect(jsonPath("$.content[0].pricePerNight").value(300000.00));
    }

    @Test
    @DisplayName("un sortBy fuera de la lista blanca da 400")
    void unknownSortIs400() throws Exception {
        doPost("/api/accommodations/search", null, search("x", "password", "asc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("la búsqueda con fechas excluye los alojamientos ya reservados")
    void searchExcludesBookedAccommodations() throws Exception {
        String host = newHost();
        String city = "Ciudad-" + UUID.randomUUID();
        long free = createInCity(host, city, "100000.00");
        long taken = createInCity(host, city, "100000.00");
        createBooking(newUser(), taken, 40, 44);

        Map<String, Object> criteria = search(city, null, null);
        criteria.put("checkIn", LocalDate.now().plusDays(41).toString());
        criteria.put("checkOut", LocalDate.now().plusDays(43).toString());

        doPost("/api/accommodations/search", null, criteria)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(free));
    }

    @Test
    @DisplayName("borrado lógico: desaparece de listados y detalle, pero conserva el historial")
    void softDeleteKeepsHistory() throws Exception {
        String host = newHost();
        String guest = newUser();
        String city = "Ciudad-" + UUID.randomUUID();
        long acc = createInCity(host, city, "100000.00");
        long booking = createBooking(guest, acc, 10, 12);
        doPost("/api/booking/" + booking + "/cancel", guest, null).andExpect(status().isOk());

        doDelete("/api/accommodations/" + acc, host).andExpect(status().isOk());

        doGet("/api/accommodations/" + acc, null).andExpect(status().isBadRequest());
        doPost("/api/accommodations/search", null, search(city, null, null))
                .andExpect(jsonPath("$.content.length()").value(0));
        doGet("/api/accommodations/host/my-accommodations", host)
                .andExpect(jsonPath("$.content[*].id").value(not(hasItem((int) acc))));
        // la reserva (cancelada) sigue en el historial del huésped
        doGet("/api/booking/" + booking, guest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        // y no se puede borrar dos veces ni reservar
        doDelete("/api/accommodations/" + acc, host).andExpect(status().isBadRequest());
        doPost("/api/booking", newUser(), bookingBody(acc, 10, 12, 2)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("no se puede borrar con reservas pendientes o confirmadas futuras")
    void cannotDeleteWithActiveBookings() throws Exception {
        String host = newHost();
        long acc = createAccommodation(host, "100000.00");
        createBooking(newUser(), acc, 10, 12);

        doDelete("/api/accommodations/" + acc, host)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.content").value("Cannot delete accommodation with active bookings"));
        doGet("/api/accommodations/" + acc, null).andExpect(status().isOk());
    }

    @Test
    @DisplayName("otro host no puede editar ni borrar un alojamiento ajeno")
    void otherHostCannotModify() throws Exception {
        long acc = createAccommodation(newHost(), "100000.00");
        String intruder = newHost();

        doPut("/api/accommodations/" + acc, intruder, accommodationBody("1.00")).andExpect(status().isBadRequest());
        doDelete("/api/accommodations/" + acc, intruder).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("editar no permite falsear la calificación ni el número de reseñas")
    void updateCannotForgeRating() throws Exception {
        String host = newHost();
        long acc = createAccommodation(host, "100000.00");
        Map<String, Object> forged = accommodationBody("120000.00");
        forged.put("averageRating", 5.0);

        doPut("/api/accommodations/" + acc, host, forged)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pricePerNight").value(120000.00))
                .andExpect(jsonPath("$.averageRating").value(0.0));
    }

    @Test
    @DisplayName("métricas: ingresos y valor promedio exactos para el host dueño")
    void metricsAreExact() throws Exception {
        String host = newHost();
        long acc = createAccommodation(host, "99.99");
        long booking = createBooking(newUser(), acc, 10, 13);
        doPost("/api/booking/" + booking + "/confirm", host, null).andExpect(status().isOk());

        doGet("/api/accommodations/" + acc + "/metrics", host)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBookings").value(1))
                .andExpect(jsonPath("$.confirmedBookings").value(1))
                .andExpect(jsonPath("$.totalRevenue").value(299.97))
                .andExpect(jsonPath("$.averageBookingValue").value(299.97));
        doGet("/api/accommodations/" + acc + "/metrics", newHost()).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("favoritos: agregar, consultar, contar para el host y quitar")
    void favorites() throws Exception {
        String host = newHost();
        String guest = newUser();
        long acc = createAccommodation(host, "100000.00");

        doPost("/api/accommodations/add-favorite/" + acc, guest, null).andExpect(status().isOk());
        doGet("/api/accommodations/is-favorite/" + acc, guest).andExpect(content().string("true"));
        doGet("/api/accommodations/favorites", guest)
                .andExpect(jsonPath("$.content[0].id").value(acc));
        doGet("/api/accommodations/host/accommodation-favorites/" + acc, host).andExpect(content().string("1"));

        doDelete("/api/accommodations/remove-favorite/" + acc, guest).andExpect(status().isOk());
        doGet("/api/accommodations/is-favorite/" + acc, guest).andExpect(content().string("false"));
    }
}
