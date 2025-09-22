package com.labndbnb.landbnb.service;

import com.labndbnb.landbnb.dto.aut_dto.*;
import com.labndbnb.landbnb.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Map<Long, User> usersById = new ConcurrentHashMap<>();
    private static final Map<String, User> usersByEmail = new ConcurrentHashMap<>();
    private static final Map<String, String> passwordResetTokens = new ConcurrentHashMap<>(); // Token -> Email
    private final AtomicLong userIdCounter = new AtomicLong();

    @Override
    public AuthResponse register(UserRegistration request) {
        return null;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        return null;
    }

    @Override
    public void sendResetPasswordEmail(String email) {

    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {

    }

    @Override
    public void changePassword(ChangePasswordRequest request) {

    }
/*
    @Override
    public AuthResponse register(UserRegistration request) {
        if (usersByEmail.containsKey(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo electrónico ya está registrado.");
        }

        User newUser = new User();
        long newId = userIdCounter.incrementAndGet();

        newUser.setId(newId);
        newUser.setEmail(request.getEmail());
        newUser.setPassword(request.getPassword());
        newUser.setNombre(request.getNombre());
        newUser.setTelefono(request.getTelefono());
        newUser.setRol(request.getRol().toUpperCase());
        newUser.setFechaNacimiento(request.getFechaNacimiento());

        usersById.put(newId, newUser);
        usersByEmail.put(newUser.getEmail(), newUser);

        // Simula la creación de un token JWT
        String token = "mock-jwt-token-for-user-" + newId;

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUserId(newUser.getId());
        response.setEmail(newUser.getEmail());
        response.setRol(newUser.getRol());

        return response;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = usersByEmail.get(request.getEmail());

        if (user == null || !user.getPassword().equals(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas.");
        }

        // Simula la creación de un token JWT
        String token = "mock-jwt-token-for-user-" + user.getId();

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setRol(user.getRol());

        return response;
    }

    @Override
    public void sendResetPasswordEmail(String email) {
        User user = usersByEmail.get(email);
        if (user == null) {

            System.out.println("Solicitud de reseteo para un email no existente: " + email);
            return;
        }

        String token = UUID.randomUUID().toString();
        passwordResetTokens.put(token, email);


        System.out.println("======================================================");
        System.out.println("Enviando correo de reseteo de contraseña a: " + email);
        System.out.println("Token: " + token);
        System.out.println("======================================================");
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        String email = passwordResetTokens.get(request.getToken());

        if (email == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido o expirado.");
        }

        User user = usersByEmail.get(email);
        if (user != null) {

            user.setPassword(request.getNewPassword());
            usersByEmail.put(email, user);
            usersById.put(user.getId(), user);
        }

        passwordResetTokens.remove(request.getToken());
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {

        User user = usersById.get(1L);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado.");
        }

        if (!user.getPassword().equals(request.getCurrentPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña actual es incorrecta.");
        }

        user.setPassword(request.getNewPassword());
        usersById.put(user.getId(), user);
        usersByEmail.put(user.getEmail(), user);

        System.out.println("Contraseña cambiada exitosamente para el usuario: " + user.getEmail());
    }

 */
}

