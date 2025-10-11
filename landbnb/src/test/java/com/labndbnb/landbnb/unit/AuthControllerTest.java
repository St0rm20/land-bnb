package com.labndbnb.landbnb.unit;

import com.labndbnb.landbnb.controller.AuthController;
import com.labndbnb.landbnb.dto.aut_dto.*;
import com.labndbnb.landbnb.dto.util_dto.ErrorResponse;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.service.definition.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Clase de pruebas unitarias para {@link AuthController}.
 *
 * Usa Mockito para aislar la lógica del controlador y simular
 * el comportamiento de {@link AuthService}.
 *
 * Se valida el comportamiento de cada endpoint expuesto:
 * <ul>
 *   <li>POST /api/auth/register</li>
 *   <li>POST /api/auth/login</li>
 *   <li>POST /api/auth/forgot-password</li>
 *   <li>POST /api/auth/reset-password</li>
 * </ul>
 *
 * Estructura de pruebas:
 * - @Nested agrupa casos por endpoint.
 * - @DisplayName mejora la legibilidad en los reportes de test.
 * - Patrón Given–When–Then aplicado en cada caso.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Controller Unit Tests")
class AuthControllerTest {

    /** Mock del servicio de autenticación. */
    @Mock
    private AuthService authService;

    /** Controlador bajo prueba, con dependencias inyectadas por Mockito. */
    @InjectMocks
    private AuthController authController;

    // ==== Datos comunes para las pruebas ====
    private UserRegistration userRegistration;
    private LoginRequest loginRequest;
    private ForgotMyPassword forgotMyPassword;
    private ResetPasswordRequest resetPasswordRequest;
    private AuthResponse authResponse;
    private InfoDto infoDto;

    /**
     * Inicializa los objetos de prueba antes de cada test.
     */
    @BeforeEach
    void setUp() {
        setupTestData();
    }

    /**
     * Crea datos de ejemplo reutilizables para las pruebas.
     */
    private void setupTestData() {
        userRegistration = new UserRegistration(
                "test@example.com",
                "Password123",
                "John",
                "Doe",
                "+573001234567",
                LocalDate.of(1990, 1, 1)
        );

        loginRequest = new LoginRequest("test@example.com", "Password123");

        forgotMyPassword = new ForgotMyPassword("test@example.com");

        resetPasswordRequest = new ResetPasswordRequest(
                "test@example.com",
                "123456",
                "NewPassword456"
        );

        authResponse = new AuthResponse("jwt-token-here");

        infoDto = new InfoDto("Success", "Operation completed successfully");
    }

    // ==========================================================
    // TESTS PARA /api/auth/register
    // ==========================================================
    @Nested
    @DisplayName("POST /api/auth/register - Registrar usuario")
    class RegisterTests {

        /**
         * Caso feliz: registro exitoso.
         * <p>
         * Given: un {@link UserRegistration} válido.<br>
         * When: se llama a {@link AuthController#register(UserRegistration)}.<br>
         * Then: retorna {@link HttpStatus#CREATED} y verifica la llamada al servicio.
         */
        @Test
        @DisplayName("Debería registrar usuario exitosamente")
        void givenValidUserRegistration_whenRegister_thenReturnCreated() {
            // Given
            when(authService.register(any(UserRegistration.class))).thenReturn(infoDto);

            // When
            ResponseEntity<?> response = authController.register(userRegistration);

            // Then
            assertAll(
                    () -> assertEquals(HttpStatus.CREATED, response.getStatusCode()),
                    () -> assertNotNull(response.getBody()),
                    () -> verify(authService, times(1)).register(any(UserRegistration.class))
            );
        }

        /**
         * Caso de error: correo ya registrado.
         * <p>
         * Given: un {@link UserRegistration} con datos inválidos.<br>
         * When: el servicio lanza {@link ExceptionAlert}.<br>
         * Then: el controlador devuelve {@link HttpStatus#BAD_REQUEST} y un {@link ErrorResponse}.
         */
        @Test
        @DisplayName("Debería retornar BAD_REQUEST cuando el registro falla")
        void givenInvalidUserRegistration_whenRegister_thenReturnBadRequest() {
            // Given
            when(authService.register(any(UserRegistration.class)))
                    .thenThrow(new ExceptionAlert("Email already in use"));

            // When
            ResponseEntity<?> response = authController.register(userRegistration);

            // Then
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                    () -> assertNotNull(response.getBody()),
                    () -> assertTrue(response.getBody() instanceof ErrorResponse)
            );
        }
    }

    // ==========================================================
    // TESTS PARA /api/auth/login
    // ==========================================================
    @Nested
    @DisplayName("POST /api/auth/login - Iniciar sesión")
    class LoginTests {

        /**
         * Caso feliz: credenciales válidas.
         * <p>
         * Given: un {@link LoginRequest} válido.<br>
         * When: se ejecuta el login correctamente.<br>
         * Then: retorna {@link HttpStatus#OK} y el {@link AuthResponse}.
         */
        @Test
        @DisplayName("Debería iniciar sesión exitosamente")
        void givenValidCredentials_whenLogin_thenReturnAuthResponse() {
            // Given
            when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

            // When
            ResponseEntity<?> response = authController.login(loginRequest);

            // Then
            assertAll(
                    () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                    () -> assertNotNull(response.getBody()),
                    () -> assertEquals(authResponse, response.getBody()),
                    () -> verify(authService, times(1)).login(any(LoginRequest.class))
            );
        }

        /**
         * Caso de error: credenciales incorrectas.
         * <p>
         * Given: credenciales inválidas.<br>
         * When: el servicio lanza {@link ExceptionAlert}.<br>
         * Then: el controlador devuelve {@link HttpStatus#UNAUTHORIZED} y un {@link ErrorResponse}.
         */
        @Test
        @DisplayName("Debería retornar UNAUTHORIZED cuando las credenciales son inválidas")
        void givenInvalidCredentials_whenLogin_thenReturnUnauthorized() {
            // Given
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new ExceptionAlert("Invalid credentials"));

            // When
            ResponseEntity<?> response = authController.login(loginRequest);

            // Then
            assertAll(
                    () -> assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode()),
                    () -> assertNotNull(response.getBody()),
                    () -> assertTrue(response.getBody() instanceof ErrorResponse)
            );
        }
    }

    // ==========================================================
    // TESTS PARA /api/auth/forgot-password
    // ==========================================================
    @Nested
    @DisplayName("POST /api/auth/forgot-password - Olvidé mi contraseña")
    class ForgotPasswordTests {

        /**
         * Caso feliz: solicitud de restablecimiento enviada correctamente.
         * <p>
         * Given: un email válido.<br>
         * When: se ejecuta {@link AuthController#forgotPassword(ForgotMyPassword)}.<br>
         * Then: retorna {@link HttpStatus#OK} y verifica la llamada al servicio.
         */
        @Test
        @DisplayName("Debería enviar email de recuperación exitosamente")
        void givenValidEmail_whenForgotPassword_thenReturnSuccess() {
            // Given
            when(authService.sendResetPasswordEmail(any(ForgotMyPassword.class))).thenReturn(infoDto);

            // When
            ResponseEntity<?> response = authController.forgotPassword(forgotMyPassword);

            // Then
            assertAll(
                    () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                    () -> assertNotNull(response.getBody()),
                    () -> verify(authService, times(1)).sendResetPasswordEmail(any(ForgotMyPassword.class))
            );
        }
    }

    // ==========================================================
    // TESTS PARA /api/auth/reset-password
    // ==========================================================
    @Nested
    @DisplayName("POST /api/auth/reset-password - Restablecer contraseña")
    class ResetPasswordTests {

        /**
         * Caso feliz: restablecimiento correcto.
         * <p>
         * Given: una solicitud válida de reseteo.<br>
         * When: el servicio ejecuta correctamente.<br>
         * Then: retorna {@link HttpStatus#OK} y se verifica la llamada al servicio.
         */
        @Test
        @DisplayName("Debería restablecer contraseña exitosamente")
        void givenValidResetRequest_whenResetPassword_thenReturnSuccess() throws ExceptionAlert {
            // Given
            when(authService.resetPassword(any(ResetPasswordRequest.class))).thenReturn(infoDto);

            // When
            ResponseEntity<?> response = authController.resetPassword(resetPasswordRequest);

            // Then
            assertAll(
                    () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                    () -> assertNotNull(response.getBody()),
                    () -> verify(authService, times(1)).resetPassword(any(ResetPasswordRequest.class))
            );
        }

        /**
         * Caso de error: token inválido o solicitud corrupta.
         * <p>
         * Given: un {@link ResetPasswordRequest} inválido.<br>
         * When: el servicio lanza {@link ExceptionAlert}.<br>
         * Then: se propaga la excepción (controlador no la maneja).
         */
        @Test
        @DisplayName("Debería propagar excepción cuando el restablecimiento falla")
        void givenInvalidResetRequest_whenResetPassword_thenPropagateException() throws ExceptionAlert {
            // Given
            when(authService.resetPassword(any(ResetPasswordRequest.class)))
                    .thenThrow(new ExceptionAlert("Invalid token"));

            // When & Then
            assertThrows(ExceptionAlert.class, () ->
                    authController.resetPassword(resetPasswordRequest)
            );
        }
    }
}
