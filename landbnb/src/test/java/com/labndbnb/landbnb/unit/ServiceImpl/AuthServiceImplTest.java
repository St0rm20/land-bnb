package com.labndbnb.landbnb.unit.ServiceImpl;

import com.labndbnb.landbnb.dto.aut_dto.*;
import com.labndbnb.landbnb.dto.user_dto.UserDto;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.model.enums.UserRole;
import com.labndbnb.landbnb.model.enums.UserStatus;
import com.labndbnb.landbnb.security.JWTutils;
import com.labndbnb.landbnb.service.definition.UserService;
import com.labndbnb.landbnb.service.implement.AuthServiceImpl;
import com.labndbnb.landbnb.service.implement.MailServiceImpl;
import com.labndbnb.landbnb.service.implement.ResetPasswordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Service Implementation Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private MailServiceImpl mailServiceImpl;

    @Mock
    private JWTutils jwtUtil;

    @Mock
    private ResetPasswordServiceImpl resetPasswordService;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserRegistration testUserRegistration;
    private LoginRequest testLoginRequest;
    private UserDto testUserDto;
    private ForgotMyPassword testForgotPassword;
    private ResetPasswordRequest testResetPasswordRequest;

    @BeforeEach
    void setUp() {
        testUserRegistration = new UserRegistration(
                "test@example.com",
                "Password123",
                "John",
                "Doe",
                "+1234567890",
                LocalDate.of(1990, 1, 1)
        );

        testLoginRequest = new LoginRequest("test@example.com", "Password123");

        testUserDto = new UserDto(
                1,
                "test@example.com",
                "John",
                "Doe",
                "+1234567890",
                UserRole.USER,
                "https://example.com/photo.jpg",
                LocalDate.of(1990, 1, 1),
                UserStatus.ACTIVE,
                "Test user bio"
        );

        testForgotPassword = new ForgotMyPassword("test@example.com");

        testResetPasswordRequest = new ResetPasswordRequest(
                "test@example.com",
                "reset-token-123",
                "NewPassword123"
        );
    }

    @Nested
    @DisplayName("Register")
    class RegisterTests {

        @Test
        @DisplayName("Should register user successfully when valid data provided")
        void shouldRegisterUser_WhenValidDataProvided() throws ExceptionAlert {
            // When
            InfoDto result = authService.register(testUserRegistration);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.message()).isEqualTo("The user has been created successfully");
            verify(userService).create(testUserRegistration);
        }

        @Test
        @DisplayName("Should propagate exception when user creation fails")
        void shouldPropagateException_WhenUserCreationFails() throws ExceptionAlert {
            // Given
            doThrow(new ExceptionAlert("Email already exists"))
                    .when(userService).create(testUserRegistration);

            // When & Then
            assertThatThrownBy(() -> authService.register(testUserRegistration))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("Email already exists");
            verify(userService).create(testUserRegistration);
        }
    }

    @Nested
    @DisplayName("Login")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully when valid credentials provided")
        void shouldLoginSuccessfully_WhenValidCredentialsProvided() throws ExceptionAlert {
            // Given
            String expectedToken = "jwt-token-123";
            Map<String, String> expectedClaims = new HashMap<>();
            expectedClaims.put("role", UserRole.USER.name());
            expectedClaims.put("email", "test@example.com");
            expectedClaims.put("userId", "1");

            when(userService.getByEmail("test@example.com")).thenReturn(testUserDto);
            when(userService.isThePasswordCorrect("test@example.com", "Password123")).thenReturn(true);
            when(jwtUtil.generateToken("test@example.com", expectedClaims)).thenReturn(expectedToken);

            // When
            AuthResponse result = authService.login(testLoginRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.token()).isEqualTo(expectedToken);
            verify(userService).getByEmail("test@example.com");
            verify(userService).isThePasswordCorrect("test@example.com", "Password123");
            verify(jwtUtil).generateToken("test@example.com", expectedClaims);
            verify(mailServiceImpl).sendSimpleEmail(
                    eq("test@example.com"),
                    eq("Nuevo inicio de sesión"),
                    contains("Se ha detectado un nuevo inicio de sesión")
            );
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowException_WhenUserNotFound() throws ExceptionAlert {
            // Given
            when(userService.getByEmail("nonexistent@example.com")).thenReturn(null);

            LoginRequest invalidLogin = new LoginRequest("nonexistent@example.com", "Password123");

            // When & Then
            assertThatThrownBy(() -> authService.login(invalidLogin))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("Credenciales inválidas");
            verify(userService).getByEmail("nonexistent@example.com");
            verify(userService, never()).isThePasswordCorrect(anyString(), anyString());
            verify(jwtUtil, never()).generateToken(anyString(), anyMap());
        }

        @Test
        @DisplayName("Should throw exception when password is incorrect")
        void shouldThrowException_WhenPasswordIsIncorrect() throws ExceptionAlert {
            // Given
            when(userService.getByEmail("test@example.com")).thenReturn(testUserDto);
            when(userService.isThePasswordCorrect("test@example.com", "WrongPassword")).thenReturn(false);

            LoginRequest wrongPasswordLogin = new LoginRequest("test@example.com", "WrongPassword");

            // When & Then
            assertThatThrownBy(() -> authService.login(wrongPasswordLogin))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("Credenciales inválidas");
            verify(userService).getByEmail("test@example.com");
            verify(userService).isThePasswordCorrect("test@example.com", "WrongPassword");
            verify(jwtUtil, never()).generateToken(anyString(), anyMap());
        }

        @Test
        @DisplayName("Should generate correct claims for JWT token")
        void shouldGenerateCorrectClaims_ForJWTToken() throws ExceptionAlert {
            // Given
            String expectedToken = "jwt-token-123";

            when(userService.getByEmail("test@example.com")).thenReturn(testUserDto);
            when(userService.isThePasswordCorrect("test@example.com", "Password123")).thenReturn(true);
            when(jwtUtil.generateToken(eq("test@example.com"), any(Map.class))).thenReturn(expectedToken);

            // When
            AuthResponse result = authService.login(testLoginRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.token()).isEqualTo(expectedToken);

            verify(jwtUtil).generateToken(eq("test@example.com"), argThat(claims ->
                    claims.get("role").equals(UserRole.USER.name()) &&
                            claims.get("email").equals("test@example.com") &&
                            claims.get("userId").equals("1")
            ));
        }

        @Test
        @DisplayName("Should allow login for inactive users with correct credentials")
        void shouldAllowLogin_ForInactiveUsersWithCorrectCredentials() throws ExceptionAlert {
            // Given
            UserDto inactiveUser = new UserDto(
                    2,
                    "inactive@example.com",
                    "Inactive",
                    "User",
                    "+1234567890",
                    UserRole.USER,
                    null,
                    LocalDate.of(1990, 1, 1),
                    UserStatus.INACTIVE, // Usuario INACTIVO
                    "Inactive user"
            );

            String expectedToken = "inactive-user-token";

            when(userService.getByEmail("inactive@example.com")).thenReturn(inactiveUser);
            when(userService.isThePasswordCorrect("inactive@example.com", "Password123")).thenReturn(true);
            when(jwtUtil.generateToken(eq("inactive@example.com"), any(Map.class))).thenReturn(expectedToken);

            LoginRequest inactiveUserLogin = new LoginRequest("inactive@example.com", "Password123");

            // When
            AuthResponse result = authService.login(inactiveUserLogin);

            // Then
            // Tu código ACTUAL permite login a usuarios inactivos
            assertThat(result).isNotNull();
            assertThat(result.token()).isEqualTo(expectedToken);

            verify(userService).getByEmail("inactive@example.com");
            verify(userService).isThePasswordCorrect("inactive@example.com", "Password123");
            verify(jwtUtil).generateToken(eq("inactive@example.com"), any(Map.class));
        }
    }

    @Nested
    @DisplayName("Send Reset Password Email")
    class SendResetPasswordEmailTests {

        @Test
        @DisplayName("Should send reset password email successfully")
        void shouldSendResetPasswordEmail_Successfully() {
            // When
            InfoDto result = authService.sendResetPasswordEmail(testForgotPassword);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.message()).isEqualTo("Please check your email for further instructions.");
            verify(resetPasswordService).sendResetPasswordEmail("test@example.com");
        }

        @Test
        @DisplayName("Should return info even when email service throws exception")
        void shouldReturnInfo_EvenWhenEmailServiceThrowsException() throws ExceptionAlert {
            // Given
            // CORREGIDO: Usando ExceptionAlert que es lo que realmente lanza tu código
            doThrow(new ExceptionAlert("Email not found"))
                    .when(resetPasswordService).sendResetPasswordEmail("test@example.com");

            // When
            InfoDto result = authService.sendResetPasswordEmail(testForgotPassword);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.message()).isEqualTo("Please check your email for further instructions.");
            verify(resetPasswordService).sendResetPasswordEmail("test@example.com");
        }
    }

    @Nested
    @DisplayName("Reset Password")
    class ResetPasswordTests {

        @Test
        @DisplayName("Should reset password successfully when valid token provided")
        void shouldResetPassword_WhenValidTokenProvided() throws ExceptionAlert {
            // Given
            when(resetPasswordService.resetPassword("test@example.com", "reset-token-123", "NewPassword123"))
                    .thenReturn(true);

            // When
            InfoDto result = authService.resetPassword(testResetPasswordRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.message()).isEqualTo("Your password has been updated successfully.");
            verify(resetPasswordService).resetPassword("test@example.com", "reset-token-123", "NewPassword123");
        }

        @Test
        @DisplayName("Should return error info when reset password fails")
        void shouldReturnErrorInfo_WhenResetPasswordFails() throws ExceptionAlert {
            // Given
            when(resetPasswordService.resetPassword("test@example.com", "invalid-token", "NewPassword123"))
                    .thenReturn(false);

            ResetPasswordRequest invalidRequest = new ResetPasswordRequest(
                    "test@example.com",
                    "invalid-token",
                    "NewPassword123"
            );

            // When
            InfoDto result = authService.resetPassword(invalidRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.message()).isEqualTo("Could not reset the password. Please ensure the email and token are correct.");
            verify(resetPasswordService).resetPassword("test@example.com", "invalid-token", "NewPassword123");
        }

        @Test
        @DisplayName("Should propagate exception when reset password service throws")
        void shouldPropagateException_WhenResetPasswordServiceThrows() throws ExceptionAlert {
            // Given
            when(resetPasswordService.resetPassword("test@example.com", "reset-token-123", "NewPassword123"))
                    .thenThrow(new ExceptionAlert("Token expired"));

            // When & Then
            assertThatThrownBy(() -> authService.resetPassword(testResetPasswordRequest))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("Token expired");
            verify(resetPasswordService).resetPassword("test@example.com", "reset-token-123", "NewPassword123");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null user DTO gracefully in login")
        void shouldHandleNullUserDTO_GracefullyInLogin() throws ExceptionAlert {
            // Given
            when(userService.getByEmail("nulluser@example.com")).thenReturn(null);

            LoginRequest nullUserLogin = new LoginRequest("nulluser@example.com", "Password123");

            // When & Then
            assertThatThrownBy(() -> authService.login(nullUserLogin))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("Credenciales inválidas");
        }

        @Test
        @DisplayName("Should handle different user roles in JWT claims")
        void shouldHandleDifferentUserRoles_InJWTClaims() throws ExceptionAlert {
            // Given
            UserDto hostUser = new UserDto(
                    2,
                    "host@example.com",
                    "Jane",
                    "Smith",
                    "+0987654321",
                    UserRole.HOST,
                    "https://example.com/host.jpg",
                    LocalDate.of(1985, 5, 15),
                    UserStatus.ACTIVE,
                    "Professional host"
            );

            LoginRequest hostLogin = new LoginRequest("host@example.com", "Password123");
            String expectedToken = "host-token-123";

            when(userService.getByEmail("host@example.com")).thenReturn(hostUser);
            when(userService.isThePasswordCorrect("host@example.com", "Password123")).thenReturn(true);
            when(jwtUtil.generateToken(eq("host@example.com"), any(Map.class))).thenReturn(expectedToken);

            // When
            AuthResponse result = authService.login(hostLogin);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.token()).isEqualTo(expectedToken);

            verify(jwtUtil).generateToken(eq("host@example.com"), argThat(claims ->
                    claims.get("role").equals(UserRole.HOST.name()) &&
                            claims.get("email").equals("host@example.com") &&
                            claims.get("userId").equals("2")
            ));
        }

        @Test
        @DisplayName("Should handle user without profile picture")
        void shouldHandleUser_WithoutProfilePicture() throws ExceptionAlert {
            // Given
            UserDto userWithoutPhoto = new UserDto(
                    3,
                    "nophoto@example.com",
                    "No",
                    "Photo",
                    "+1111111111",
                    UserRole.USER,
                    null,
                    LocalDate.of(1995, 6, 20),
                    UserStatus.ACTIVE,
                    null
            );

            String expectedToken = "nophoto-token-123";

            when(userService.getByEmail("nophoto@example.com")).thenReturn(userWithoutPhoto);
            when(userService.isThePasswordCorrect("nophoto@example.com", "Password123")).thenReturn(true);
            when(jwtUtil.generateToken(eq("nophoto@example.com"), any(Map.class))).thenReturn(expectedToken);

            LoginRequest noPhotoLogin = new LoginRequest("nophoto@example.com", "Password123");

            // When
            AuthResponse result = authService.login(noPhotoLogin);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.token()).isEqualTo(expectedToken);
            verify(userService).getByEmail("nophoto@example.com");
        }
    }
}