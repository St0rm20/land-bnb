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
import com.labndbnb.landbnb.service.implement.UserServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
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
import java.time.LocalDateTime;
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
    private com.labndbnb.landbnb.repository.BookingRepository bookingRepository;

    @Mock
    private com.labndbnb.landbnb.repository.AccommodationRepository accommodationRepository;


    private final Long httpServletRequest = 1L;

    @InjectMocks
    private UserServiceImpl userService;

    @org.junit.jupiter.api.BeforeEach
    void setCloudName() {
        org.springframework.test.util.ReflectionTestUtils.setField(userService, "cloudName", "demo");
    }

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
                "https://res.cloudinary.com/demo/image/upload/v1/new-photo.jpg",
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
            when(userRepository.existsByEmail("test@example.com")).thenReturn(false);

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
            verify(userRepository).existsByEmail("test@example.com");
            verify(userRegistrationMapper).toEntity(testUserRegistration);
            verify(passwordEncoder).encode("Password123");

            // Verifica que el usuario guardado tenga la contraseña codificada
            verify(userRepository).save(argThat(user -> user.getPassword().equals("encodedPassword123")));
        }

        @Test
        @DisplayName("Should throw exception when email is already in use")
        void shouldThrowException_WhenEmailIsAlreadyInUse() {
            // Given
            when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

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
            verify(userRepository).existsByEmail("existing@example.com");
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
                            user.getProfilePictureUrl().equals("https://res.cloudinary.com/demo/image/upload/v1/new-photo.jpg") &&
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
                    "https://res.cloudinary.com/demo/image/upload/v1/photo.jpg",
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
        @DisplayName("Should throw when current password is incorrect")
        void shouldThrow_WhenCurrentPasswordIncorrect() {
            // Given
            setupValidToken();
            when(passwordEncoder.matches(testChangePasswordRequest.currentPassword(), testUser.getPassword()))
                    .thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> userService.changePassword(testChangePasswordRequest, httpServletRequest))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("Current password is incorrect");
            verify(userRepository, never()).save(any(User.class));
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
        @DisplayName("Host without active bookings can delete: account and accommodations are deactivated")
        void shouldDeleteHostAccount_AndDeactivateAccommodations() throws ExceptionAlert {
            User hostUser = User.builder().id(1L).email("host@example.com").role(UserRole.HOST).build();
            Accommodation acc = Accommodation.builder().id(5L).host(hostUser).build();
            setupValidTokenWithUser(hostUser);
            when(bookingRepository.existsByAccommodation_Host_IdAndEndDateAfterAndBookingStatusIn(
                    eq(1L), any(LocalDateTime.class), any())).thenReturn(false);
            when(accommodationRepository.findAllByHostIdAndActiveTrue(1L)).thenReturn(List.of(acc));

            InfoDto result = userService.delete(httpServletRequest);

            assertThat(result.message()).isEqualTo("Your account has been deleted");
            assertThat(acc.getActive()).isFalse();
            verify(accommodationRepository).saveAll(List.of(acc));
            verify(userRepository).save(argThat(u -> u.getStatus() == UserStatus.INACTIVE));
        }

        @Test
        @DisplayName("Host with active bookings cannot delete the account")
        void shouldRejectHostDeletion_WhenActiveBookingsExist() throws ExceptionAlert {
            User hostUser = User.builder().id(1L).email("host@example.com").role(UserRole.HOST).build();
            setupValidTokenWithUser(hostUser);
            when(bookingRepository.existsByAccommodation_Host_IdAndEndDateAfterAndBookingStatusIn(
                    eq(1L), any(LocalDateTime.class), any())).thenReturn(true);

            assertThatThrownBy(() -> userService.delete(httpServletRequest))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessageContaining("active bookings");

            verify(userRepository, never()).save(any(User.class));
            verify(accommodationRepository, never()).saveAll(any());
        }
    }

    @Nested
    @DisplayName("Get Active User")
    class GetActiveUserTests {

        @Test
        @DisplayName("Should return user when id belongs to an active user")
        void shouldReturnUser_WhenActive() throws ExceptionAlert {
            setupValidToken();

            User result = userService.getActiveUser(httpServletRequest);

            assertThat(result).isEqualTo(testUser);
            verify(userRepository).findByIdAndStatus(1L, UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should throw exception when user id is null")
        void shouldThrowException_WhenIdIsNull() {
            assertThatThrownBy(() -> userService.getActiveUser(null))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("User not found");
            verify(userRepository, never()).findByIdAndStatus(any(), any());
        }

        @Test
        @DisplayName("Should throw exception when user not found or inactive")
        void shouldThrowException_WhenUserNotFound() {
            when(userRepository.findByIdAndStatus(999L, UserStatus.ACTIVE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getActiveUser(999L))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessage("User not found");
        }
    }

    @Nested
    @DisplayName("Seguridad de sesión y datos de perfil")
    class SessionSecurityTests {

        @Test
        @DisplayName("cambiar la contraseña invalida los JWT emitidos hasta ese momento")
        void changePasswordRevokesTokens() throws ExceptionAlert {
            setupValidToken();
            when(passwordEncoder.matches(testChangePasswordRequest.currentPassword(), testUser.getPassword())).thenReturn(true);
            when(passwordEncoder.encode(anyString())).thenReturn("new-hash");
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            userService.changePassword(testChangePasswordRequest, httpServletRequest);

            assertThat(testUser.getTokensValidAfter()).isAfter(before);
        }

        @Test
        @DisplayName("borrar la cuenta invalida los JWT")
        void deleteAccountRevokesTokens() throws ExceptionAlert {
            setupValidToken();

            userService.delete(httpServletRequest);

            assertThat(testUser.getTokensValidAfter()).isNotNull();
        }

        @Test
        @DisplayName("la foto de perfil debe estar alojada en nuestro Cloudinary")
        void rejectsForeignPhotoUrl() throws ExceptionAlert {
            setupValidToken();
            UserUpdateDto dto = new UserUpdateDto("A", "B", "+573001234567",
                    "https://evil.example.com/tracker.png", "d", "bio", "1990-01-01");

            assertThatThrownBy(() -> userService.update(dto, httpServletRequest))
                    .isInstanceOf(ExceptionAlert.class)
                    .hasMessageContaining("uploaded through the platform");
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("una URL que solo imita el prefijo de Cloudinary tampoco vale")
        void rejectsLookalikePhotoUrl() throws ExceptionAlert {
            setupValidToken();
            UserUpdateDto dto = new UserUpdateDto("A", "B", "+573001234567",
                    "https://res.cloudinary.com.evil.example/demo/x.png", "d", "bio", "1990-01-01");

            assertThatThrownBy(() -> userService.update(dto, httpServletRequest))
                    .isInstanceOf(ExceptionAlert.class);
        }

        @Test
        @DisplayName("sin foto (null/vacía) se permite quitarla")
        void allowsClearingPhoto() throws ExceptionAlert {
            setupValidToken();
            UserUpdateDto dto = new UserUpdateDto("A", "B", "+573001234567",
                    "", "d", "bio", "1990-01-01");

            userService.update(dto, httpServletRequest);

            assertThat(testUser.getProfilePictureUrl()).isNull();
        }

        @Test
        @DisplayName("registerLogin: el primer login avisa y guarda lastLoginAt")
        void firstLoginNotifies() throws ExceptionAlert {
            setupValidToken();
            testUser.setLastLoginAt(null);

            assertThat(userService.registerLogin(httpServletRequest)).isTrue();
            assertThat(testUser.getLastLoginAt()).isNotNull();
        }

        @Test
        @DisplayName("registerLogin: no vuelve a avisar dentro de las 24 h")
        void recentLoginDoesNotNotify() throws ExceptionAlert {
            setupValidToken();
            testUser.setLastLoginAt(LocalDateTime.now().minusHours(2));

            assertThat(userService.registerLogin(httpServletRequest)).isFalse();
        }

        @Test
        @DisplayName("registerLogin: avisa de nuevo pasadas las 24 h")
        void oldLoginNotifiesAgain() throws ExceptionAlert {
            setupValidToken();
            testUser.setLastLoginAt(LocalDateTime.now().minusHours(25));

            assertThat(userService.registerLogin(httpServletRequest)).isTrue();
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

    private void setupValidToken() {
        setupValidTokenWithUser(testUser);
    }

    private void setupValidTokenWithUser(User user) {
        when(userRepository.findByIdAndStatus(httpServletRequest, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
    }
}
