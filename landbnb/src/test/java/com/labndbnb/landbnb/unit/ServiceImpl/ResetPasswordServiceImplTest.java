package com.labndbnb.landbnb.unit.ServiceImpl;

import com.labndbnb.landbnb.dto.user_dto.UserDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.exceptions.TokenIncorrect;
import com.labndbnb.landbnb.model.ResetPassword;
import com.labndbnb.landbnb.model.User;
import com.labndbnb.landbnb.model.enums.UserRole;
import com.labndbnb.landbnb.model.enums.UserStatus;
import com.labndbnb.landbnb.repository.ResetPasswordRepository;
import com.labndbnb.landbnb.service.definition.UserService;
import com.labndbnb.landbnb.service.implement.MailServiceImpl;
import com.labndbnb.landbnb.service.implement.ResetPasswordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResetPasswordServiceImpl")
class ResetPasswordServiceImplTest {

    @Mock private ResetPasswordRepository passwordRepository;
    @Mock private MailServiceImpl emailService;
    @Mock private UserService userService;
    @Mock private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks private ResetPasswordServiceImpl service;

    private UserDto userDto;
    private User user;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(1, "a@b.com", "Ana", "Perez", "3001234567",
                UserRole.USER, null, null, UserStatus.ACTIVE, null);
        user = User.builder().id(1L).email("a@b.com").password("old-hash").build();
    }

    private ResetPassword validToken(int attempts) {
        ResetPassword t = new ResetPassword();
        t.setToken("hashed-code");
        t.setUser(user);
        t.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        t.setAttempts(attempts);
        return t;
    }

    @Nested
    @DisplayName("sendResetPasswordEmail")
    class Send {

        @Test
        @DisplayName("genera código de 6 dígitos, guarda solo el hash y envía el código por correo")
        void sendsSixDigitCodeAndStoresHash() throws ExceptionAlert {
            when(userService.getByEmail("a@b.com")).thenReturn(userDto);
            when(userService.getActiveUser(1L)).thenReturn(user);
            when(passwordEncoder.encode(anyString())).thenReturn("hashed-code");

            assertThat(service.sendResetPasswordEmail("a@b.com")).isTrue();

            ArgumentCaptor<String> encoded = ArgumentCaptor.forClass(String.class);
            verify(passwordEncoder).encode(encoded.capture());
            assertThat(encoded.getValue()).matches("\\d{6}");

            ArgumentCaptor<ResetPassword> saved = ArgumentCaptor.forClass(ResetPassword.class);
            verify(passwordRepository).deleteAllByUserId(1L);
            verify(passwordRepository).save(saved.capture());
            assertThat(saved.getValue().getToken()).isEqualTo("hashed-code");
            assertThat(saved.getValue().getUser()).isSameAs(user);
            assertThat(saved.getValue().getExpiresAt()).isAfter(LocalDateTime.now());

            ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
            verify(emailService).sendSimpleEmail(eq("a@b.com"), anyString(), body.capture());
            assertThat(body.getValue()).contains(encoded.getValue());
        }

        @Test
        @DisplayName("lanza ExceptionAlert si el correo no existe")
        void throwsWhenUserUnknown() throws ExceptionAlert {
            when(userService.getByEmail("x@y.com")).thenReturn(null);

            assertThatThrownBy(() -> service.sendResetPasswordEmail("x@y.com"))
                    .isInstanceOf(ExceptionAlert.class);
            verify(passwordRepository, never()).save(any());
            verify(emailService, never()).sendSimpleEmail(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("resetPassword")
    class Reset {

        @Test
        @DisplayName("con código válido cambia la contraseña de la entidad real y marca el token como usado")
        void resetsPasswordOnValidCode() throws ExceptionAlert {
            ResetPassword token = validToken(0);
            when(userService.getByEmail("a@b.com")).thenReturn(userDto);
            when(passwordRepository.findByUserId(1L)).thenReturn(token);
            when(passwordEncoder.matches("123456", "hashed-code")).thenReturn(true);
            when(userService.getActiveUser(1L)).thenReturn(user);
            when(passwordEncoder.encode("NewPass1")).thenReturn("new-hash");

            assertThat(service.resetPassword("a@b.com", "123456", "NewPass1")).isTrue();

            // Se guarda la MISMA entidad cargada de BD, no una reconstruida desde el DTO
            ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
            verify(userService).save(saved.capture());
            assertThat(saved.getValue()).isSameAs(user);
            assertThat(saved.getValue().getPassword()).isEqualTo("new-hash");
            assertThat(saved.getValue().getTokensValidAfter()).isNotNull(); // cierra sesiones abiertas
            assertThat(token.isUsed()).isTrue();
        }

        @Test
        @DisplayName("con código incorrecto incrementa los intentos y lanza TokenIncorrect")
        void wrongCodeIncrementsAttempts() throws ExceptionAlert {
            ResetPassword token = validToken(2);
            when(userService.getByEmail("a@b.com")).thenReturn(userDto);
            when(passwordRepository.findByUserId(1L)).thenReturn(token);
            when(passwordEncoder.matches("000000", "hashed-code")).thenReturn(false);

            assertThatThrownBy(() -> service.resetPassword("a@b.com", "000000", "NewPass1"))
                    .isInstanceOf(TokenIncorrect.class);

            assertThat(token.getAttempts()).isEqualTo(3);
            verify(passwordRepository).save(token);
            verify(userService, never()).save(any());
        }

        @Test
        @DisplayName("tras 5 intentos fallidos rechaza incluso el código correcto")
        void locksAfterMaxAttempts() throws ExceptionAlert {
            ResetPassword token = validToken(5);
            when(userService.getByEmail("a@b.com")).thenReturn(userDto);
            when(passwordRepository.findByUserId(1L)).thenReturn(token);

            assertThatThrownBy(() -> service.resetPassword("a@b.com", "123456", "NewPass1"))
                    .isInstanceOf(TokenIncorrect.class);

            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(userService, never()).save(any());
        }

        @Test
        @DisplayName("rechaza tokens expirados")
        void rejectsExpired() throws ExceptionAlert {
            ResetPassword token = validToken(0);
            token.setExpiresAt(LocalDateTime.now().minusMinutes(1));
            when(userService.getByEmail("a@b.com")).thenReturn(userDto);
            when(passwordRepository.findByUserId(1L)).thenReturn(token);

            assertThatThrownBy(() -> service.resetPassword("a@b.com", "123456", "NewPass1"))
                    .isInstanceOf(TokenIncorrect.class);
            verify(userService, never()).save(any());
        }

        @Test
        @DisplayName("rechaza tokens ya usados")
        void rejectsUsed() throws ExceptionAlert {
            ResetPassword token = validToken(0);
            token.setUsed(true);
            when(userService.getByEmail("a@b.com")).thenReturn(userDto);
            when(passwordRepository.findByUserId(1L)).thenReturn(token);

            assertThatThrownBy(() -> service.resetPassword("a@b.com", "123456", "NewPass1"))
                    .isInstanceOf(TokenIncorrect.class);
        }

        @Test
        @DisplayName("correo inexistente da el mismo error que un código incorrecto (no revela cuentas)")
        void unknownEmailLooksLikeWrongCode() throws ExceptionAlert {
            when(userService.getByEmail("x@y.com")).thenReturn(null);

            assertThatThrownBy(() -> service.resetPassword("x@y.com", "123456", "NewPass1"))
                    .isInstanceOf(TokenIncorrect.class);
        }
    }
}
