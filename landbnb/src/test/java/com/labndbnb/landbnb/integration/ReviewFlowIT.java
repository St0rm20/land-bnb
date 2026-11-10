package com.labndbnb.landbnb.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Integración: reseñas")
class ReviewFlowIT extends IntegrationTestSupport {

    /** Crea alojamiento + reserva confirmada. Devuelve {accommodationId, bookingId}. */
    private long[] confirmedBooking(String host, String guest) throws Exception {
        long acc = createAccommodation(host, "100000.00");
        long booking = createBooking(guest, acc, 10, 12);
        doPost("/api/booking/" + booking + "/confirm", host, null).andExpect(status().isOk());
        return new long[]{acc, booking};
    }

    private Map<String, Object> review(long bookingId, int rating) {
        return Map.of("bookingId", bookingId, "rating", rating, "text", "Muy buena estadía");
    }

    @Test
    @DisplayName("el huésped reseña una reserva confirmada y se actualiza la calificación")
    void reviewUpdatesRating() throws Exception {
        String host = newHost();
        String guest = newUser();
        long[] ids = confirmedBooking(host, guest);

        doGet("/api/comments/can-comment/" + ids[0], guest).andExpect(content().string("true"));
        doPost("/api/comments", guest, review(ids[1], 4)).andExpect(status().isCreated());

        doGet("/api/accommodations/" + ids[0], null).andExpect(jsonPath("$.averageRating").value(4.0));
        doGet("/api/comments/can-comment/" + ids[0], guest).andExpect(content().string("false"));
        doGet("/api/comments/accommodation/" + ids[0], null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].calificacion").value(4));
    }

    @Test
    @DisplayName("no se puede reseñar dos veces la misma reserva")
    void cannotReviewTwice() throws Exception {
        String guest = newUser();
        long[] ids = confirmedBooking(newHost(), guest);
        doPost("/api/comments", guest, review(ids[1], 5)).andExpect(status().isCreated());

        doPost("/api/comments", guest, review(ids[1], 5)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("calificación fuera de 1-5 da 400 de validación")
    void ratingOutOfRange() throws Exception {
        String guest = newUser();
        long[] ids = confirmedBooking(newHost(), guest);

        doPost("/api/comments", guest, review(ids[1], 6)).andExpect(status().isBadRequest());
        doPost("/api/comments", guest, review(ids[1], 0)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("no se puede reseñar una reserva PENDING ni una ajena")
    void invalidReviewers() throws Exception {
        String host = newHost();
        String guest = newUser();
        long acc = createAccommodation(host, "100000.00");
        long pending = createBooking(guest, acc, 10, 12);

        doPost("/api/comments", guest, review(pending, 5)).andExpect(status().isBadRequest());

        doPost("/api/booking/" + pending + "/confirm", host, null).andExpect(status().isOk());
        doPost("/api/comments", newUser(), review(pending, 5)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("el host responde una sola vez; otro host no puede responder")
    void hostRepliesOnce() throws Exception {
        String host = newHost();
        String guest = newUser();
        long[] ids = confirmedBooking(host, guest);
        doPost("/api/comments", guest, review(ids[1], 5)).andExpect(status().isCreated());
        long reviewId = body(doGet("/api/comments/accommodation/" + ids[0], null)).get("content").get(0).get("id").asLong();

        doGet("/api/comments/can-reply/" + reviewId, host).andExpect(content().string("true"));
        doPost("/api/comments/reply", newHost(), Map.of("message", "intruso", "commentId", reviewId))
                .andExpect(status().isBadRequest());
        doPost("/api/comments/reply", guest, Map.of("message", "soy huésped", "commentId", reviewId))
                .andExpect(status().isForbidden());

        doPost("/api/comments/reply", host, Map.of("message", "¡Gracias!", "commentId", reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.respuestaAnfitrion").value("¡Gracias!"));
        doPost("/api/comments/reply", host, Map.of("message", "otra vez", "commentId", reviewId))
                .andExpect(status().isBadRequest());
        doGet("/api/comments/can-reply/" + reviewId, host).andExpect(content().string("false"));
    }

    @Test
    @DisplayName("la reseña de una reserva solo la ven su autor y el host")
    void reviewByBookingOwnership() throws Exception {
        String host = newHost();
        String guest = newUser();
        long[] ids = confirmedBooking(host, guest);
        doPost("/api/comments", guest, review(ids[1], 5)).andExpect(status().isCreated());

        doGet("/api/comments/booking/" + ids[1], guest).andExpect(status().isOk());
        doGet("/api/comments/booking/" + ids[1], host).andExpect(status().isOk());
        doGet("/api/comments/booking/" + ids[1], newUser()).andExpect(status().isBadRequest());
        doGet("/api/comments/booking/" + ids[1], null).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("borrar la reseña recalcula la calificación; solo su autor puede")
    void deleteReviewRecalculates() throws Exception {
        String host = newHost();
        String guest = newUser();
        long[] ids = confirmedBooking(host, guest);
        doPost("/api/comments", guest, review(ids[1], 5)).andExpect(status().isCreated());
        long reviewId = body(doGet("/api/comments/accommodation/" + ids[0], null)).get("content").get(0).get("id").asLong();

        doDelete("/api/comments/delete/" + reviewId, newUser()).andExpect(status().isBadRequest());
        doDelete("/api/comments/delete/" + reviewId, guest).andExpect(status().isOk());

        doGet("/api/accommodations/" + ids[0], null).andExpect(jsonPath("$.averageRating").value(0.0));
    }
}
