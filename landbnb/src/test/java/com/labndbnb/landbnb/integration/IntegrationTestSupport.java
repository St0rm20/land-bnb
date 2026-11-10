package com.labndbnb.landbnb.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.labndbnb.landbnb.service.implement.MailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Base de los tests de integración: arranca la app completa (seguridad real, JPA real,
 * BD H2 en memoria). Solo se simula el envío de correo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {

    protected static final String PASSWORD = "Password123";

    @Autowired protected MockMvc mvc;
    @Autowired protected ObjectMapper json;

    @MockitoBean protected MailServiceImpl mailService;

    // ------------------------------------------------------------------ http
    protected MockHttpServletRequestBuilder withToken(MockHttpServletRequestBuilder b, String token) {
        return token == null ? b : b.header("Authorization", "Bearer " + token);
    }

    protected ResultActions doGet(String url, String token) throws Exception {
        return mvc.perform(withToken(get(url), token));
    }

    protected ResultActions doPost(String url, String token, Object body) throws Exception {
        MockHttpServletRequestBuilder b = withToken(post(url), token);
        if (body != null) b.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
        return mvc.perform(b);
    }

    protected ResultActions doPut(String url, String token, Object body) throws Exception {
        return mvc.perform(withToken(put(url), token)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)));
    }

    protected ResultActions doDelete(String url, String token) throws Exception {
        return mvc.perform(withToken(delete(url), token));
    }

    protected JsonNode body(ResultActions r) throws Exception {
        return json.readTree(r.andReturn().getResponse().getContentAsString());
    }

    // ----------------------------------------------------------------- users
    protected String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@test.com";
    }

    protected void register(String email, String password) throws Exception {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("email", email);
        req.put("password", password);
        req.put("name", "Test");
        req.put("lastName", "User");
        req.put("phoneNumber", "+573001234567");
        req.put("birthDate", "1990-01-01");
        doPost("/api/auth/register", null, req)
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated());
    }

    protected ResultActions loginRaw(String email, String password) throws Exception {
        return doPost("/api/auth/login", null, Map.of("email", email, "password", password));
    }

    protected String login(String email, String password) throws Exception {
        return body(loginRaw(email, password)).get("token").asText();
    }

    /** Registra un usuario nuevo y devuelve su token. */
    protected String newUser(String email) throws Exception {
        register(email, PASSWORD);
        return login(email, PASSWORD);
    }

    protected String newUser() throws Exception {
        return newUser(uniqueEmail());
    }

    protected String newHost() throws Exception {
        String token = newUser();
        doPost("/api/users/become-host", token, null)
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        return token;
    }

    // ---------------------------------------------------------- accommodations
    protected Map<String, Object> accommodationBody(String price) {
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("title", "Casa " + UUID.randomUUID().toString().substring(0, 6));
        b.put("description", "Una casa de prueba");
        b.put("city", "Bogotá");
        b.put("address", "Calle 1 # 2-3");
        b.put("latitude", 4.7110);
        b.put("longitude", -74.0721);
        b.put("pricePerNight", new java.math.BigDecimal(price));
        b.put("maxCapacity", 4);
        b.put("services", java.util.List.of("WiFi"));
        return b;
    }

    protected long createAccommodation(String hostToken, String price) throws Exception {
        ResultActions r = doPost("/api/accommodations", hostToken, accommodationBody(price))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated());
        Matcher m = Pattern.compile("ID (\\d+)").matcher(body(r).get("message").asText());
        if (!m.find()) throw new IllegalStateException("No se encontró el id en la respuesta");
        return Long.parseLong(m.group(1));
    }

    // ---------------------------------------------------------------- bookings
    protected Map<String, Object> bookingBody(long accommodationId, int inDays, int outDays, int guests) {
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("accommodationId", accommodationId);
        b.put("checkIn", LocalDate.now().plusDays(inDays).toString());
        b.put("checkOut", LocalDate.now().plusDays(outDays).toString());
        b.put("numberOfGuests", guests);
        return b;
    }

    protected long createBooking(String token, long accommodationId, int inDays, int outDays) throws Exception {
        ResultActions r = doPost("/api/booking", token, bookingBody(accommodationId, inDays, outDays, 2))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated());
        return body(r).get("id").asLong();
    }
}
