package com.labndbnb.landbnb.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Integración: contraseñas, sesiones y cuenta")
class PasswordAndSessionIT extends IntegrationTestSupport {

    /** El iat del JWT tiene precisión de segundos: se espera para que el token viejo sea anterior al cambio. */
    private void passSecondBoundary() throws InterruptedException {
        Thread.sleep(1100);
    }

    private String requestResetCode(String email) throws Exception {
        doPost("/api/auth/forgot-password", null, Map.of("email", email)).andExpect(status().isOk());
        ArgumentCaptor<String> text = ArgumentCaptor.forClass(String.class);
        verify(mailService).sendSimpleEmail(eq(email), eq("Password Reset Request"), text.capture());
        return text.getValue().replaceAll("\\D+", "");
    }

    private Map<String, String> reset(String email, String code, String newPassword) {
        return Map.of("email", email, "token", code, "newPassword", newPassword);
    }

    // ------------------------------------------------------- cambio de contraseña
    @Test
    @DisplayName("cambiar la contraseña invalida el token anterior; el login nuevo funciona")
    void changePasswordRevokesOldToken() throws Exception {
        String email = uniqueEmail();
        String oldToken = newUser(email);
        passSecondBoundary();

        doPost("/api/users/change-password", oldToken,
                Map.of("currentPassword", PASSWORD, "newPassword", "NuevaPass456")).andExpect(status().isOk());

        doGet("/api/users/profile", oldToken).andExpect(status().isUnauthorized());
        loginRaw(email, PASSWORD).andExpect(status().isUnauthorized());
        String newToken = login(email, "NuevaPass456");
        doGet("/api/users/profile", newToken).andExpect(status().isOk());
    }

    @Test
    @DisplayName("con la contraseña actual incorrecta no cambia nada y el token sigue válido")
    void wrongCurrentPassword() throws Exception {
        String token = newUser();

        doPost("/api/users/change-password", token,
                Map.of("currentPassword", "Incorrecta1", "newPassword", "NuevaPass456"))
                .andExpect(status().isBadRequest());

        doGet("/api/users/profile", token).andExpect(status().isOk());
    }

    // ------------------------------------------------------- recuperación
    @Test
    @DisplayName("recuperar contraseña con el código del correo; cierra las sesiones abiertas")
    void resetPasswordFlow() throws Exception {
        String email = uniqueEmail();
        String oldToken = newUser(email);
        String code = requestResetCode(email);
        assertThat(code).matches("\\d{6}");
        passSecondBoundary();

        doPost("/api/auth/reset-password", null, reset(email, code, "Recuperada789")).andExpect(status().isOk());

        doGet("/api/users/profile", oldToken).andExpect(status().isUnauthorized());
        doGet("/api/users/profile", login(email, "Recuperada789")).andExpect(status().isOk());
        // el código es de un solo uso
        doPost("/api/auth/reset-password", null, reset(email, code, "OtraMas111")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("tras 5 códigos incorrectos el código correcto deja de servir")
    void resetCodeLocksAfterFiveFailures() throws Exception {
        String email = uniqueEmail();
        register(email, PASSWORD);
        String code = requestResetCode(email);
        String wrong = code.equals("000000") ? "111111" : "000000";

        for (int i = 0; i < 5; i++) {
            doPost("/api/auth/reset-password", null, reset(email, wrong, "Recuperada789"))
                    .andExpect(status().isBadRequest());
        }

        doPost("/api/auth/reset-password", null, reset(email, code, "Recuperada789"))
                .andExpect(status().isBadRequest());
        loginRaw(email, PASSWORD).andExpect(status().isOk()); // la contraseña original sigue intacta
    }

    @Test
    @DisplayName("forgot-password responde igual para correos que no existen (no revela cuentas)")
    void forgotPasswordDoesNotLeakAccounts() throws Exception {
        doPost("/api/auth/forgot-password", null, Map.of("email", "no-existe-" + uniqueEmail()))
                .andExpect(status().isOk());

        verify(mailService, never()).sendSimpleEmail(anyString(), eq("Password Reset Request"), anyString());
    }

    @Test
    @DisplayName("reset con un correo inexistente da el mismo error que un código incorrecto")
    void resetForUnknownEmailLooksLikeWrongCode() throws Exception {
        doPost("/api/auth/reset-password", null, reset("no-existe-" + uniqueEmail(), "123456", "Recuperada789"))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------- borrar cuenta
    @Test
    @DisplayName("borrar la cuenta invalida el token y bloquea el login")
    void deleteAccount() throws Exception {
        String email = uniqueEmail();
        String token = newUser(email);
        passSecondBoundary();

        doDelete("/api/users/delete-account", token).andExpect(status().isOk());

        doGet("/api/users/profile", token).andExpect(status().isUnauthorized());
        loginRaw(email, PASSWORD).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("un host con reservas activas no puede borrar su cuenta")
    void hostWithActiveBookingsCannotDelete() throws Exception {
        String host = newHost();
        createBooking(newUser(), createAccommodation(host, "100000.00"), 10, 12);

        doDelete("/api/users/delete-account", host).andExpect(status().isBadRequest());
        doGet("/api/users/profile", host).andExpect(status().isOk());
    }

    @Test
    @DisplayName("un host sin reservas activas puede borrarla y sus alojamientos se desactivan")
    void hostCanDeleteAndAccommodationsGetDeactivated() throws Exception {
        String host = newHost();
        long acc = createAccommodation(host, "100000.00");
        doGet("/api/accommodations/" + acc, null).andExpect(status().isOk());

        doDelete("/api/users/delete-account", host).andExpect(status().isOk());

        doGet("/api/accommodations/" + acc, null).andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------- perfil y login
    @Test
    @DisplayName("la foto de perfil debe ser de nuestro Cloudinary")
    void profilePhotoMustBeOurs() throws Exception {
        String token = newUser();
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("name", "Ana");
        body.put("lastName", "Pérez");
        body.put("phoneNumber", "+573001234567");
        body.put("dateBirth", "1990-01-01");

        body.put("photoProfile", "https://evil.example.com/tracker.png");
        doPut("/api/users/profile", token, body).andExpect(status().isBadRequest());

        body.put("photoProfile", "https://res.cloudinary.com/demo/image/upload/v1/yo.jpg");
        doPut("/api/users/profile", token, body).andExpect(status().isOk());
        doGet("/api/users/profile", token)
                .andExpect(jsonPath("$.profilePictureUrl").value("https://res.cloudinary.com/demo/image/upload/v1/yo.jpg"));
    }

    @Test
    @DisplayName("el aviso de inicio de sesión se envía una sola vez en 24 h")
    void loginEmailIsThrottled() throws Exception {
        String email = uniqueEmail();
        register(email, PASSWORD);

        login(email, PASSWORD);
        login(email, PASSWORD);
        login(email, PASSWORD);

        verify(mailService, times(1)).sendSimpleEmail(eq(email), eq("Nuevo inicio de sesión"), anyString());
    }
}
