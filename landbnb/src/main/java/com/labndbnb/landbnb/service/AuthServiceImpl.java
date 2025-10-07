package com.labndbnb.landbnb.service;

import com.labndbnb.landbnb.dto.aut_dto.*;
import com.labndbnb.landbnb.dto.user_dto.UserDto;
import com.labndbnb.landbnb.model.User;
import com.labndbnb.landbnb.repository.UserRepository;
import com.labndbnb.landbnb.security.JWTutils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {


    private final UserService userService;
    private final MailService mailService;
    private final JWTutils  jwtUtil;

    @Override
    public Boolean register(UserRegistration request) throws Exception {
        return userService.create(request);
    }

    @Override
    public AuthResponse login(LoginRequest request) throws Exception {
        UserDto userDto = userService.getByEmail(request.email());
        if (userDto == null) {
            throw new Exception("Credenciales inválidas"); // Mensaje genérico por seguridad
        }

        if (!userService.isThePasswordCorrect(request.email(), request.password())) {
            throw new Exception("Credenciales inválidas");
        }
        Map<String, String> claims = new HashMap<>();
        claims.put("role", userDto.userRole().name());
        claims.put("email", userDto.email());
        claims.put("userId", userDto.id().toString());

        String token = jwtUtil.generateToken(userDto.email(), claims);

        mailService.sendSimpleEmail(userDto.email(), "Nuevo inicio de sesión", "Hola, "+ userDto.name() + " Se ha detectado un nuevo inicio de sesión en tu cuenta.");
        return new AuthResponse(
                token
        );
    }

    @Override
    public void sendResetPasswordEmail(ForgotMyPassword email) {

        try {
            UserDto userDto = userService.getByEmail(email.email());
            if (userDto != null) {
                String resetToken = jwtUtil.generateToken(userDto.email(), Map.of("userId", userDto.id().toString()));

                String resetLink = "http://localhost:4200/reset-password?token=" + resetToken;

                String emailBody = "Hola, " + userDto.name() + "\n\n" +
                        "Hemos recibido una solicitud para restablecer tu contraseña. " +
                        "Haz clic en el siguiente enlace para restablecer tu contraseña:\n" +
                        resetLink + "\n\n" +
                        "Si no solicitaste este cambio, puedes ignorar este correo electrónico.\n\n" +
                        "Saludos,\n" +
                        "El equipo de LabNDBnb";

                mailService.sendSimpleEmail(userDto.email(), "Restablecimiento de contraseña", emailBody);
            }
        } catch (Exception e) {
            // Manejo de excepciones (opcional)
            e.printStackTrace();
        }
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {

    }

    @Override
    public void changePassword(ChangePasswordRequest request) {

    }

}

