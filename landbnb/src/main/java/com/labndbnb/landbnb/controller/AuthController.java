package com.labndbnb.landbnb.controller;

import com.labndbnb.landbnb.dto.aut_dto.*;
import com.labndbnb.landbnb.dto.util_dto.ErrorResponse;
import com.labndbnb.landbnb.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid UserRegistration userRegistration) {
        try {
            authService.register(userRegistration);
            return ResponseEntity.ok(HttpStatus.OK);

        } catch (Exception e) {
            ErrorResponse errorResponse = ErrorResponse.of(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest) {
        try {
            AuthResponse authResponse = authService.login(loginRequest);
            return ResponseEntity.ok().body(authResponse);
        } catch (Exception e) {
            ErrorResponse errorResponse = ErrorResponse.of(
                    HttpStatus.UNAUTHORIZED,
                    "Credenciales inválidas"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam @Valid ForgotMyPassword email) {
        authService.sendResetPasswordEmail(email);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
