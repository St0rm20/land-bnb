package com.labndbnb.landbnb.unit.ServiceImpl;

import com.labndbnb.landbnb.dto.aut_dto.ChangePasswordRequest;
import com.labndbnb.landbnb.dto.aut_dto.UserRegistration;
import com.labndbnb.landbnb.dto.user_dto.UserDto;
import com.labndbnb.landbnb.dto.user_dto.UserUpdateDto;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.exceptions.EmailAlreadyInUse;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.mappers.auth.UserRegistrationMapper;
import com.labndbnb.landbnb.mappers.user.UserDtoMapper;
import com.labndbnb.landbnb.model.Accommodation;
import com.labndbnb.landbnb.model.User;
import com.labndbnb.landbnb.model.enums.UserRole;
import com.labndbnb.landbnb.model.enums.UserStatus;
import com.labndbnb.landbnb.repository.UserRepository;
import com.labndbnb.landbnb.security.JWTutils;
import com.labndbnb.landbnb.service.implement.UserServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Implementation Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserRegistrationMapper userRegistrationMapper;

    @Mock
    private UserDtoMapper userDtoMapper;

    @Mock
    private UserRepository userRepository;

    @Mock(lenient = true)
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JWTutils jwtutils;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserRegistration testUserRegistration;
    private UserDto testUserDto;
    private UserUpdateDto testUserUpdateDto;
    private ChangePasswordRequest testChangePasswordRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("John")
                .lastName("Doe")
                .phoneNumber("+1234567890")
                .password("encodedPassword123")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .profilePictureUrl("https://example.com/photo.jpg")
                .bio("Test bio")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();

        testUserRegistration = new UserRegistration(
                "test@example.com",
                "Password123",
                "John",
                "Doe",
                "+1234567890",
                LocalDate.of(1990, 1, 1)
        );

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
                "Test bio"
        );

        testUserUpdateDto = new UserUpdateDto(
                "John Updated",
                "Doe Updated",
                "+0987654321",
                "https://example.com/new-photo.jpg",
                "Updated description",
                "Updated bio",
                "1990-01-01"
        );

        testChangePasswordRequest = new ChangePasswordRequest(
                "CurrentPassword123",
                "NewPassword456"
        );
    }

    @Nested
    @DisplayName("Create User")
    class CreateUserTests {

        // Clase anidada CreateUserTests

        @Test
        @DisplayName("Should create user successfully when email is not in use")
        void shouldCreateUser_WhenEmailIsNotInUse() throws ExceptionAlert {

            // **AÑADIDO:** Crea un User con la contraseña PLANA del DTO.
            // Este es el objeto que el mapper *debería* devolver, listo para ser codificado por el servicio.
            User userWithPlainPassword = User.builder()
                    .id(1L)
                    .email("test@example.com")
                    .name("John")
                    .lastName("Doe")
                    .phoneNumber("+1234567890")
                    // Usamos la contraseña PLANA aquí
                    .password(testUserRegistration.password())
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .profilePictureUrl("https://example.com/photo.jpg")
                    .bio("Test bio")
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .build();

            // Given
            when(userRepository.existsByEmailAndStatus("test@example.com", UserStatus.ACTIVE)).thenReturn(false);

            // **CORRECCIÓN:** El mapper devuelve el usuario con la contraseña PLANA
            when(userRegistrationMapper.toEntity(testUserRegistration)).thenReturn(userWithPlainPassword);

            // Mockea el codificador con la contraseña PLANA de entrada
            when(passwordEncoder.encode("Password123")).thenReturn("encodedPassword123");

            // Cuando el servicio guarda el usuario, devuelve el usuario final (testUser ya tiene la contraseña codificada)
            // Usamos any() aquí para simular el éxito, pero idealmente usarías argThat() para verificar la contraseña codificada.
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            Boolean result = userService.create(testUserRegistration);

            // Then
            assertThat(result).isTrue();
            verify(userRepository).existsByEmailAndStatus("test@example.com", UserStatus.ACTIVE);
            verify(userRegistrationMapper).toEntity(testUserRegistration);
            verify(passwordEncoder).encode("Password123");

            // Verifica que el usuario guardado tenga la contraseña codificada
            verify(userRepository).save(argThat(user -> user.getPassword().equals("encodedPassword123")));
        }

        @Test
        @DisplayName("Should throw exception when email is already in use")
        void shouldThrowException_WhenEmailIsAlreadyInUse() {
            // Given
            when(userRepository.existsByEmailAndStatus("existing@example.com", UserStatus.ACTIVE)).thenReturn(true);

            UserRegistration existingUser = new UserRegistration(
                    "existing@example.com",
                    "Password123",
                    "Existing",
                    "User",
                    "+1234567890",
                    LocalDate.of(1990, 1, 1)
            );

            // When & Then
            assertThatThrownBy(() -> userService.create(existingUser))
                    .isInstanceOf(EmailAlreadyInUse.class)
                    .hasMessage("Email already in use");
            verify(userRepository).existsByEmailAndStatus("existing@example.com", UserStatus.ACTIVE);
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Get User")
    class GetUserTests {

        @Test
        @DisplayName("Should return user when valid ID provided")
        void shouldReturnUser_WhenValidIdProvided() throws ExceptionAlert {
            // Given
            String userId = "1";
            when(userRepository.existsByIdAndStatus(1L, UserStatus.ACTIVE)).thenReturn(true);
            when(userRepository.getUsersByIdAndStatus(1L, UserStatus.ACTIVE)).thenReturn(testUser);
            when(userDtoMapper.toDto(testUser)).thenReturn(testUserDto);

            // When
            UserDto result = userService.get(userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(testUserDto);
            verify(userRepository).existsByIdAndStatus(1L, UserStatus.ACTIVE);
            verify(userRepository).getUsersByIdAndStatus(1L, UserStatus.ACTIVE);
            verify(userDtoMapper).toDto(testUser);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowException_WhenUserNotFound() {
            // Given
            String invalidUserId = "999";
            when(userRepository.existsByIdAndStatus(999L, UserStatus.ACTIVE)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> userService.get(invalidUserId))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("User with id 999 not found");
            verify(userRepository).existsByIdAndStatus(999L, UserStatus.ACTIVE);
            verify(userRepository, never()).getUsersByIdAndStatus(anyLong(), any());
        }
    }

    @Nested
    @DisplayName("Delete User")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user when valid ID provided")
        void shouldDeleteUser_WhenValidIdProvided() throws ExceptionAlert {
            // Given
            String userId = "1";
            when(userRepository.existsByIdAndStatus(1L, UserStatus.ACTIVE)).thenReturn(true);

            // When
            userService.delete(userId);

            // Then
            verify(userRepository).existsByIdAndStatus(1L, UserStatus.ACTIVE);
            verify(userRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when user not found for deletion")
        void shouldThrowException_WhenUserNotFoundForDeletion() {
            // Given
            String invalidUserId = "999";
            when(userRepository.existsByIdAndStatus(999L, UserStatus.ACTIVE)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> userService.delete(invalidUserId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User with id 999 not found");
            verify(userRepository).existsByIdAndStatus(999L, UserStatus.ACTIVE);
            verify(userRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("Update User")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user profile successfully")
        void shouldUpdateUserProfile_Successfully() throws ExceptionAlert {
            // Given
            setupValidToken();
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            InfoDto result = userService.update(testUserUpdateDto, httpServletRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.message()).isEqualTo("Profile updated successfully");

            verify(userRepository).save(argThat(user ->
                    user.getName().equals("John Updated") &&
                            user.getLastName().equals("Doe Updated") &&
                            user.getPhoneNumber().equals("+0987654321") &&
                            user.getProfilePictureUrl().equals("https://example.com/new-photo.jpg") &&
                            user.getDateOfBirth().equals(LocalDate.of(1990, 1, 1))
            ));
        }

        @Test
        @DisplayName("Should update bio for host users")
        void shouldUpdateBio_ForHostUsers() throws ExceptionAlert {
            // Given
            User hostUser = User.builder()
                    .id(1L)
                    .email("host@example.com")
                    .role(UserRole.HOST)
                    .bio("Old bio")
                    .build();

            setupValidTokenWithUser(hostUser);
            when(userRepository.save(any(User.class))).thenReturn(hostUser);

            UserUpdateDto updateWithBio = new UserUpdateDto(
                    "Host",
                    "User",
                    "+1234567890",
                    "https://example.com/photo.jpg",
                    "Description",
                    "New bio for host",
                    "1990-01-01"
            );

            // When
            InfoDto result = userService.update(updateWithBio, httpServletRequest);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).save(argThat(user ->
                    user.getBio().equals("New bio for host")
            ));
        }
    }

    @Nested
    @DisplayName("Get User By Email")
    class GetUserByEmailTests {

        @Test
        @DisplayName("Should return user when email exists")
        void shouldReturnUser_WhenEmailExists() throws ExceptionAlert {
            // Given
            when(userRepository.existsByEmailAndStatus("test@example.com", UserStatus.ACTIVE)).thenReturn(true);
            when(userRepository.findByEmailAndStatus("test@example.com", UserStatus.ACTIVE))
                    .thenReturn(Optional.of(testUser));
            when(userDtoMapper.toDto(testUser)).thenReturn(testUserDto);

            // When
            UserDto result = userService.getByEmail("test@example.com");

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(testUserDto);
            verify(userRepository).existsByEmailAndStatus("test@example.com", UserStatus.ACTIVE);
            verify(userRepository).findByEmailAndStatus("test@example.com", UserStatus.ACTIVE);
            verify(userDtoMapper).toDto(testUser);
        }

        @Test
        @DisplayName("Should return null when email does not exist")
        void shouldReturnNull_WhenEmailDoesNotExist() throws ExceptionAlert {
            // Given
            when(userRepository.existsByEmailAndStatus("nonexistent@example.com", UserStatus.ACTIVE)).thenReturn(false);

            // When
            UserDto result = userService.getByEmail("nonexistent@example.com");

            // Then
            assertThat(result).isNull();
            verify(userRepository).existsByEmailAndStatus("nonexistent@example.com", UserStatus.ACTIVE);
            verify(userRepository, never()).findByEmailAndStatus(anyString(), any());
        }
    }

    @Nested
    @DisplayName("Password Verification")
    class PasswordVerificationTests {

        @Test
        @DisplayName("Should return true when password is correct")
        void shouldReturnTrue_WhenPasswordIsCorrect() throws ExceptionAlert {
            // Given
            when(userRepository.findByEmailAndStatus("test@example.com", UserStatus.ACTIVE))
                    .thenReturn(Optional.of(testUser));

            // Use lenient mocking or handle multiple calls
            lenient().when(passwordEncoder.matches("CorrectPassword123", "encodedPassword123")).thenReturn(true);

            // When
            Boolean result = userService.isThePasswordCorrect("test@example.com", "CorrectPassword123");

            // Then
            assertThat(result).isTrue();
            verify(userRepository).findByEmailAndStatus("test@example.com", UserStatus.ACTIVE);
            // Don't verify exact number of matches calls since the implementation calls it twice
        }

        @Test
        @DisplayName("Should return false when password is incorrect")
        void shouldReturnFalse_WhenPasswordIsIncorrect() throws ExceptionAlert {
            // Given
            when(userRepository.findByEmailAndStatus("test@example.com", UserStatus.ACTIVE))
                    .thenReturn(Optional.of(testUser));

            // Use lenient mocking
            lenient().when(passwordEncoder.matches("WrongPassword", "encodedPassword123")).thenReturn(false);

            // When
            Boolean result = userService.isThePasswordCorrect("test@example.com", "WrongPassword");

            // Then
            assertThat(result).isFalse();
            verify(userRepository).findByEmailAndStatus("test@example.com", UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowException_WhenUserNotFound() {
            // Given
            when(userRepository.findByEmailAndStatus("nonexistent@example.com", UserStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.isThePasswordCorrect("nonexistent@example.com", "Password123"))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("User with email nonexistent@example.com not found");
            verify(userRepository).findByEmailAndStatus("nonexistent@example.com", UserStatus.ACTIVE);
            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Change Password")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should return error when token is expired")
        void shouldReturnError_WhenTokenIsExpired() throws ExceptionAlert {
            // Given
            String expiredToken = "expired.jwt.token";
            when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer " + expiredToken);
            when(jwtutils.parseJwt(expiredToken)).thenThrow(new ExpiredJwtException(null, null, "Token expired"));

            // When
            InfoDto result = userService.changePassword(testChangePasswordRequest, httpServletRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.message()).isEqualTo("Token expired");

            verify(httpServletRequest).getHeader("Authorization");
            verify(jwtutils).parseJwt(expiredToken);
        }
    }

    @Nested
    @DisplayName("Become Host")
    class BecomeHostTests {

        @Test
        @DisplayName("Should convert user to host successfully")
        void shouldConvertUserToHost_Successfully() throws ExceptionAlert {
            // Given
            setupValidToken();
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            InfoDto result = userService.becomeHost(httpServletRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.message()).isEqualTo("You are now a host");

            verify(userRepository).save(argThat(user ->
                    user.getRole().equals(UserRole.HOST)
            ));
        }

        @Test
        @DisplayName("Should return info when user is already a host")
        void shouldReturnInfo_WhenUserIsAlreadyHost() throws ExceptionAlert {
            // Given
            User hostUser = User.builder()
                    .id(1L)
                    .email("host@example.com")
                    .role(UserRole.HOST)
                    .build();

            setupValidTokenWithUser(hostUser);

            // When
            InfoDto result = userService.becomeHost(httpServletRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.message()).isEqualTo("You are already a host");

            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Delete Account")
    class DeleteAccountTests {

        @Test
        @DisplayName("Should deactivate user account successfully")
        void shouldDeactivateUserAccount_Successfully() throws ExceptionAlert {
            // Given
            setupValidToken();
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            InfoDto result = userService.delete(httpServletRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.message()).isEqualTo("Your account has been deleted");

            verify(userRepository).save(argThat(user ->
                    user.getStatus().equals(UserStatus.INACTIVE)
            ));
        }

        @Test
        @DisplayName("Should return error when host tries to delete account")
        void shouldReturnError_WhenHostTriesToDeleteAccount() throws ExceptionAlert {
            // Given
            User hostUser = User.builder()
                    .id(1L)
                    .email("host@example.com")
                    .role(UserRole.HOST)
                    .build();

            setupValidTokenWithUser(hostUser);

            // When
            InfoDto result = userService.delete(httpServletRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.message()).isEqualTo("Hosts cannot delete their accounts");

            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Get User From Request")
    class GetUserFromRequestTests {

        @Test
        @DisplayName("Should return user when valid token provided")
        void shouldReturnUser_WhenValidTokenProvided() throws ExceptionAlert {
            // Given
            setupValidToken();

            // When
            User result = userService.getUserFromRequest(httpServletRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(testUser);
            verify(httpServletRequest).getHeader("Authorization");
            verify(jwtutils).parseJwt("valid.jwt.token");
            verify(userRepository).findByIdAndStatus(1L, UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should throw exception when authorization header is missing")
        void shouldThrowException_WhenAuthorizationHeaderIsMissing() {
            // Given
            when(httpServletRequest.getHeader("Authorization")).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> userService.getUserFromRequest(httpServletRequest))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("Missing or invalid Authorization header");
            verify(httpServletRequest).getHeader("Authorization");
            verify(jwtutils, never()).parseJwt(anyString());
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowException_WhenUserNotFound() {
            // Given
            String validToken = "valid.jwt.token";
            Claims claims = mock(Claims.class);
            Jws<Claims> jws = mock(Jws.class);

            when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(jwtutils.parseJwt(validToken)).thenReturn(jws);
            when(jws.getPayload()).thenReturn(claims);
            when(claims.get("userId", String.class)).thenReturn("999");
            when(userRepository.findByIdAndStatus(999L, UserStatus.ACTIVE)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.getUserFromRequest(httpServletRequest))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("User not found");
            verify(httpServletRequest).getHeader("Authorization");
            verify(jwtutils).parseJwt(validToken);
            verify(userRepository).findByIdAndStatus(999L, UserStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("Find Favorites By User ID")
    class FindFavoritesByUserIdTests {

        @Test
        @DisplayName("Should return paginated favorites for user")
        void shouldReturnPaginatedFavorites_ForUser() {
            // Given
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            Page<Accommodation> favoritesPage = new PageImpl<>(List.of(mock(Accommodation.class)));

            when(userRepository.findFavoritesByUserId(userId, pageable)).thenReturn(favoritesPage);

            // When
            Page<Accommodation> result = userService.findFavoritesByUserId(userId, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(favoritesPage);
            verify(userRepository).findFavoritesByUserId(userId, pageable);
        }
    }

    private void setupValidToken() throws ExceptionAlert {
        setupValidTokenWithUser(testUser);
    }

    private void setupValidTokenWithUser(User user) throws ExceptionAlert {
        String validToken = "valid.jwt.token";
        Claims claims = mock(Claims.class);
        Jws<Claims> jws = mock(Jws.class);

        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtutils.parseJwt(validToken)).thenReturn(jws);
        when(jws.getPayload()).thenReturn(claims);
        when(claims.get("userId", String.class)).thenReturn(String.valueOf(user.getId()));
        when(userRepository.findByIdAndStatus(user.getId(), UserStatus.ACTIVE)).thenReturn(Optional.of(user));
    }
}