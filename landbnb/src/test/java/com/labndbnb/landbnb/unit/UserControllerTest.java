package com.labndbnb.landbnb.unit;

import com.labndbnb.landbnb.controller.UserController;
import com.labndbnb.landbnb.dto.aut_dto.ChangePasswordRequest;
import com.labndbnb.landbnb.dto.user_dto.UserDto;
import com.labndbnb.landbnb.dto.user_dto.UserUpdateDto;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.model.enums.UserRole;
import com.labndbnb.landbnb.model.enums.UserStatus;
import com.labndbnb.landbnb.service.definition.UserService;
import jakarta.servlet.http.HttpServletRequest;
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
 * Clase de pruebas unitarias para el controlador UserController.
 *
 * Se emplea Mockito para simular la capa de servicio y validar que los métodos
 * del controlador respondan correctamente según los casos definidos.
 *
 * Reglas aplicadas:
 * - Cada caso de prueba sigue la estructura Given-When-Then.
 * - Se usan mocks para aislar dependencias externas.
 * - No se prueban tokens ni seguridad, ya que las pruebas unitarias
 *   se enfocan en la lógica del controlador, no en la autenticación.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Unit Tests")
class UserControllerTest {

    // ---------------------------------------------------------------
    // Dependencias simuladas
    // ---------------------------------------------------------------
    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Mock
    private HttpServletRequest request;

    // ---------------------------------------------------------------
    // Objetos de prueba comunes
    // ---------------------------------------------------------------
    private UserDto userDto;
    private UserUpdateDto userUpdateDto;
    private ChangePasswordRequest changePasswordRequest;
    private InfoDto infoDto;

    // ---------------------------------------------------------------
    // Configuración inicial antes de cada prueba
    // ---------------------------------------------------------------
    @BeforeEach
    void setUp() {
        setupTestData();
        // No se requiere configuración de token o encabezados reales
    }

    /**
     * Crea los datos base que se utilizarán en los diferentes casos de prueba.
     */
    private void setupTestData() {
        userDto = new UserDto(
                1,
                "usuario@example.com",
                "Nombre",
                "Apellido",
                "3208499853",
                UserRole.USER,
                "https://miurl.com/foto-profile.jpg",
                LocalDate.of(2005, 11, 16),
                UserStatus.ACTIVE,
                "Descripción de usuario"
        );

        userUpdateDto = new UserUpdateDto(
                "Nombre",
                "Apellido",
                "3208499853",
                "https://miurl.com/foto-profile.jpg",
                "Descripción de usuario",
                "Texto adicional de biografía",
                "2005-11-16"
        );

        changePasswordRequest = new ChangePasswordRequest(
                "Password123",
                "NewPassword789"
        );

        infoDto = new InfoDto("Success", "Operación completada exitosamente");
    }

    // ===============================================================
    // BLOQUES DE PRUEBA ANIDADOS (organización por endpoint)
    // ===============================================================

    @Nested
    @DisplayName("GET /api/users/profile - Obtener perfil")
    class GetProfileTests {

        /**
         * Caso: el usuario autenticado solicita su perfil.
         *
         * Regla: Dado un usuario válido (Given), cuando se llama al método getProfile (When),
         * el controlador debe retornar el perfil del usuario con código HTTP 200 (Then).
         */
        @Test
        @DisplayName("Debe retornar el perfil del usuario")
        void givenAuthenticatedUser_whenGetProfile_thenReturnUserProfile() throws ExceptionAlert {
            when(userService.getUser(any(HttpServletRequest.class))).thenReturn(userDto);

            ResponseEntity<?> response = userController.getProfile(request);

            assertAll(
                    () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                    () -> assertEquals(userDto, response.getBody()),
                    () -> verify(userService, times(1)).getUser(any(HttpServletRequest.class))
            );
        }
    }

    @Nested
    @DisplayName("PUT /api/users/profile - Actualizar perfil")
    class UpdateProfileTests {

        /**
         * Caso: el usuario envía datos válidos para actualizar su perfil.
         *
         * Regla: Dado un objeto UserUpdateDto válido (Given), cuando se llama a updateProfile (When),
         * el controlador debe retornar una respuesta exitosa (Then).
         */
        @Test
        @DisplayName("Debe actualizar el perfil correctamente")
        void givenValidData_whenUpdateProfile_thenReturnSuccess() throws ExceptionAlert {
            when(userService.update(any(UserUpdateDto.class), any(HttpServletRequest.class)))
                    .thenReturn(infoDto);

            ResponseEntity<?> response = userController.updateProfile(userUpdateDto, request);

            assertAll(
                    () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                    () -> assertEquals(infoDto, response.getBody()),
                    () -> verify(userService, times(1)).update(any(UserUpdateDto.class), any(HttpServletRequest.class))
            );
        }
    }

    @Nested
    @DisplayName("POST /api/users/change-password - Cambiar contraseña")
    class ChangePasswordTests {

        /**
         * Caso: el usuario envía una solicitud válida para cambiar su contraseña.
         *
         * Regla: Dado un objeto ChangePasswordRequest con datos válidos (Given),
         * cuando se llama a changePassword (When),
         * el controlador debe devolver un mensaje de éxito con código HTTP 200 (Then).
         */
        @Test
        @DisplayName("Debe cambiar la contraseña correctamente")
        void givenValidPasswordChange_whenChangePassword_thenReturnSuccess() {
            when(userService.changePassword(any(ChangePasswordRequest.class), any(HttpServletRequest.class)))
                    .thenReturn(infoDto);

            ResponseEntity<?> response = userController.changePassword(changePasswordRequest, request);

            assertAll(
                    () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                    () -> assertEquals(infoDto, response.getBody()),
                    () -> verify(userService, times(1))
                            .changePassword(any(ChangePasswordRequest.class), any(HttpServletRequest.class))
            );
        }
    }

    @Nested
    @DisplayName("POST /api/users/become-host - Convertirse en host")
    class BecomeHostTests {

        /**
         * Caso: el usuario solicita cambiar su rol a host.
         *
         * Regla: Dado un usuario activo (Given), cuando se llama a becomeHost (When),
         * el controlador debe retornar una respuesta de éxito (Then).
         */
        @Test
        @DisplayName("Debe convertir el usuario en host exitosamente")
        void givenRegularUser_whenBecomeHost_thenReturnSuccess() throws ExceptionAlert {
            when(userService.becomeHost(any(HttpServletRequest.class))).thenReturn(infoDto);

            ResponseEntity<?> response = userController.becomeHost(request);

            assertAll(
                    () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                    () -> assertEquals(infoDto, response.getBody()),
                    () -> verify(userService, times(1)).becomeHost(any(HttpServletRequest.class))
            );
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/delete-account - Eliminar cuenta")
    class DeleteAccountTests {

        /**
         * Caso: el usuario solicita eliminar su cuenta.
         *
         * Regla: Dado un usuario válido (Given), cuando se llama a deleteAccount (When),
         * el controlador debe responder con un mensaje de confirmación y código HTTP 200 (Then).
         */
        @Test
        @DisplayName("Debe eliminar la cuenta correctamente")
        void givenValidUser_whenDeleteAccount_thenReturnSuccess() throws ExceptionAlert {
            when(userService.delete(any(HttpServletRequest.class))).thenReturn(infoDto);

            ResponseEntity<?> response = userController.deleteAccount(request);

            assertAll(
                    () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                    () -> assertEquals(infoDto, response.getBody()),
                    () -> verify(userService, times(1)).delete(any(HttpServletRequest.class))
            );
        }
    }
}
