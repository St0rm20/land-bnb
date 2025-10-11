package com.labndbnb.landbnb.service.implement;

import com.labndbnb.landbnb.dto.aut_dto.*;
import com.labndbnb.landbnb.dto.user_dto.UserDto;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.security.JWTutils;
import com.labndbnb.landbnb.service.definition.AuthService;
import com.labndbnb.landbnb.service.definition.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {


    private final UserService userService;
    private final MailServiceImpl mailServiceImpl;
    private final JWTutils  jwtUtil;
    private final ResetPasswordServiceImpl  resetPasswordService;

    @Override
    public InfoDto register(UserRegistration request) throws ExceptionAlert {
        userService.create(request);
        mailServiceImpl.sendSimpleEmail(request.email(), "Nuevo registro", "Hola, "+ request.name() + " Se ha registrado tu correo en Land bnb.");
        return new InfoDto("Register successful", "The user has been created successfully");
    }

    @Override
    public AuthResponse login(LoginRequest request) throws ExceptionAlert {
        UserDto userDto = userService.getByEmail(request.email());
        if (userDto == null) {
            throw new ExceptionAlert("Credenciales inválidas"); // Mensaje genérico por seguridad
        }

        if (!userService.isThePasswordCorrect(request.email(), request.password())) {
            throw new ExceptionAlert("Credenciales inválidas");
        }
        Map<String, String> claims = new HashMap<>();
        claims.put("role", userDto.userRole().name());
        claims.put("email", userDto.email());
        claims.put("userId", userDto.id().toString());

        String token = jwtUtil.generateToken(userDto.email(), claims);

        mailServiceImpl.sendSimpleEmail(userDto.email(), "Nuevo inicio de sesión", "Hola, "+ userDto.name() + " Se ha detectado un nuevo inicio de sesión en tu cuenta.");
        return new AuthResponse(
                token
        );
    }

    @Override
    public InfoDto sendResetPasswordEmail(ForgotMyPassword email) {
        try {
            resetPasswordService.sendResetPasswordEmail(email.email());

        } catch (ExceptionAlert e) {
            e.printStackTrace();

        }
        return new InfoDto("If the email exists, a reset code has been sent", "Please check your email for further instructions.");
    }

    @Override
    public InfoDto resetPassword(ResetPasswordRequest request) throws ExceptionAlert {
        if( resetPasswordService.resetPassword(request.email(), request.token(), request.newPassword())){
            return new InfoDto("Password reset successful", "Your password has been updated successfully.");
        }

        return  new InfoDto("Error resetting password", "Could not reset the password. Please ensure the email and token are correct.");
    }



}

