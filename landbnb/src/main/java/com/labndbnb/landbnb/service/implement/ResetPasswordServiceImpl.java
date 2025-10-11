package com.labndbnb.landbnb.service.implement;

import com.labndbnb.landbnb.dto.user_dto.UserDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.exceptions.TokenIncorrect;
import com.labndbnb.landbnb.mappers.user.UserDtoMapper;
import com.labndbnb.landbnb.model.ResetPassword;
import com.labndbnb.landbnb.model.User;
import com.labndbnb.landbnb.repository.ResetPasswordRepository;
import com.labndbnb.landbnb.service.definition.ResetPasswordService;
import com.labndbnb.landbnb.service.definition.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ResetPasswordServiceImpl implements ResetPasswordService {

    private final ResetPasswordRepository passwordRepository;
    private final MailServiceImpl emailServiceImpl;
    private final UserService userService;
    private final UserDtoMapper userDtoMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    Logger log = org.slf4j.LoggerFactory.getLogger(ResetPasswordServiceImpl.class);

    @Override
    public Boolean sendResetPasswordEmail(String email) throws ExceptionAlert {

        UserDto user = userService.getByEmail(email);
        if (user == null) {
            throw new ExceptionAlert("User not found with email: " + email);
        }


        String token = String.valueOf(100000 + new java.util.Random().nextInt(900000));

        ResetPassword resetToken = new ResetPassword();
        resetToken.setToken(passwordEncoder.encode(token));
        resetToken.setUser(userDtoMapper.toEntity(user));
        resetToken.setExpiresAt(java.time.LocalDateTime.now().plusMinutes(15));
        resetToken.setUsed(false);

        passwordRepository.deleteAllByUserId(user.id());
        passwordRepository.save(resetToken);
        log.info("user {}" , user.id());
        String subject = "Password Reset Request";
        String message = "Use this code to reset your password: " + token;
        emailServiceImpl.sendSimpleEmail(email, subject, message);
        return true;
    }

    @Override
    public Boolean resetPassword(String email,String token, String newPassword) throws ExceptionAlert {
        UserDto user = userService.getByEmail(email);
        if (user == null) {
            return false;
        }
        ResetPassword resetToken = passwordRepository.findByUserId(user.id());
        if (resetToken == null || resetToken.getExpiresAt().isBefore(java.time.LocalDateTime.now()) || resetToken.isUsed() || !passwordEncoder.matches(token, resetToken.getToken())) {
            throw new TokenIncorrect();
        }

        boolean isTokenValid = passwordEncoder.matches(token, resetToken.getToken());
        if (!isTokenValid) {
            throw new TokenIncorrect();
        }
        User userEntity = userDtoMapper.toEntity(user);
        userEntity.setPassword(passwordEncoder.encode(newPassword));
        return true;
    }
}
