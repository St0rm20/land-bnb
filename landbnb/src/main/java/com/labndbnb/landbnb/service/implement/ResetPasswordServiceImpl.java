package com.labndbnb.landbnb.service.implement;

import com.labndbnb.landbnb.dto.user_dto.UserDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.exceptions.TokenIncorrect;
import com.labndbnb.landbnb.model.ResetPassword;
import com.labndbnb.landbnb.model.User;
import com.labndbnb.landbnb.repository.ResetPasswordRepository;
import com.labndbnb.landbnb.service.definition.MailService;
import com.labndbnb.landbnb.service.definition.ResetPasswordService;
import com.labndbnb.landbnb.service.definition.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ResetPasswordServiceImpl implements ResetPasswordService {

    private static final int MAX_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ResetPasswordRepository passwordRepository;
    private final MailService emailServiceImpl;
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Boolean sendResetPasswordEmail(String email) throws ExceptionAlert {

        UserDto userDto = userService.getByEmail(email);
        if (userDto == null) {
            throw new ExceptionAlert("User not found with email: " + email);
        }
        User user = userService.getActiveUser(userDto.id().longValue());

        String token = String.valueOf(100000 + RANDOM.nextInt(900000));

        ResetPassword resetToken = new ResetPassword();
        resetToken.setToken(passwordEncoder.encode(token));
        resetToken.setUser(user);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        resetToken.setUsed(false);

        passwordRepository.deleteAllByUserId(user.getId());
        passwordRepository.save(resetToken);
        emailServiceImpl.sendSimpleEmail(email, "Password Reset Request",
                "Use this code to reset your password: " + token);
        return true;
    }

    @Override
    @Transactional(noRollbackFor = TokenIncorrect.class)
    public Boolean resetPassword(String email, String token, String newPassword) throws ExceptionAlert {
        UserDto userDto = userService.getByEmail(email);
        if (userDto == null) {
            // Misma respuesta que un código incorrecto para no revelar qué correos existen
            throw new TokenIncorrect();
        }
        ResetPassword resetToken = passwordRepository.findByUserId(userDto.id().longValue());

        if (resetToken == null || resetToken.isUsed()
                || resetToken.getExpiresAt().isBefore(LocalDateTime.now())
                || resetToken.getAttempts() >= MAX_ATTEMPTS) {
            throw new TokenIncorrect();
        }

        if (!passwordEncoder.matches(token, resetToken.getToken())) {
            resetToken.setAttempts(resetToken.getAttempts() + 1);
            passwordRepository.save(resetToken);
            throw new TokenIncorrect();
        }

        User user = userService.getActiveUser(userDto.id().longValue());
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setTokensValidAfter(LocalDateTime.now()); // cierra las sesiones abiertas
        userService.save(user);

        resetToken.setUsed(true);
        passwordRepository.save(resetToken);
        return true;
    }
}
