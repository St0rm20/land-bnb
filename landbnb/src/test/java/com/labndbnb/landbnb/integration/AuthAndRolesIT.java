package com.labndbnb.landbnb.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Integración: autenticación y roles (seguridad real)")
class AuthAndRolesIT extends IntegrationTestSupport {

    @Test
    @DisplayName("registro, login y acceso al perfil con el token")
    void registerLoginAndProfile() throws Exception {
        String email = uniqueEmail();
        String token = newUser(email);

        doGet("/api/users/profile", token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.userRole").value("USER"));
    }

    @Test
    @DisplayName("registrar dos veces el mismo correo da 409")
    void duplicateEmailIs409() throws Exception {
        String email = uniqueEmail();
        register(email, PASSWORD);

        doPost("/api/auth/register", null, Map.of("email", email, "password", PASSWORD, "name", "T",
                "lastName", "U", "phoneNumber", "+573001234567", "birthDate", "1990-01-01"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("contraseña incorrecta da 401")
    void wrongPasswordIs401() throws Exception {
        String email = uniqueEmail();
        register(email, PASSWORD);

        loginRaw(email, "OtraPassword1").andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("sin token un endpoint protegido responde 401")
    void noTokenIs401() throws Exception {
        doGet("/api/users/profile", null).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("un token falso responde 401")
    void garbageTokenIs401() throws Exception {
        doGet("/api/users/profile", "esto.no.es.un.jwt").andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("endpoints públicos funcionan sin token")
    void publicEndpointsNeedNoToken() throws Exception {
        doGet("/api/accommodations", null).andExpect(status().isOk());
    }

    @Test
    @DisplayName("un USER no puede crear alojamientos: 403")
    void userCannotCreateAccommodation() throws Exception {
        String user = newUser();

        doPost("/api/accommodations", user, accommodationBody("100000.00")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("un USER no puede ver las métricas ni los alojamientos de host: 403")
    void userCannotUseHostEndpoints() throws Exception {
        String user = newUser();

        doGet("/api/accommodations/host/my-accommodations", user).andExpect(status().isForbidden());
        doGet("/api/accommodations/1/metrics", user).andExpect(status().isForbidden());
        doGet("/api/booking/host", user).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("un USER no puede confirmar reservas: 403")
    void userCannotConfirmBookings() throws Exception {
        String user = newUser();

        doPost("/api/booking/1/confirm", user, null).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("solo un HOST puede borrar imágenes; un USER recibe 403")
    void onlyHostCanDeleteImages() throws Exception {
        String user = newUser();

        mvc.perform(withToken(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/images").param("id", "landbnb/x"), user))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("subir una imagen exige autenticación y rechaza archivos que no son imagen")
    void imageUploadRules() throws Exception {
        MockMultipartFile html = new MockMultipartFile("file", "x.html", "text/html", "<script>".getBytes());

        mvc.perform(multipart("/api/images").file(html)).andExpect(status().isUnauthorized());

        String user = newUser();
        mvc.perform(withToken(multipart("/api/images").file(html), user))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("un USER puede volverse HOST y entonces sí crea alojamientos")
    void becomeHostThenCreate() throws Exception {
        String token = newHost();

        long id = createAccommodation(token, "150000.00");

        assertThat(id).isPositive();
    }
}
