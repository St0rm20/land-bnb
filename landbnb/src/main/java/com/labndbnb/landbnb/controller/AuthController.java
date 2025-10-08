package com.labndbnb.landbnb.controller;

import com.labndbnb.landbnb.dto.aut_dto.*;
import com.labndbnb.landbnb.dto.util_dto.ErrorResponse;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.service.definition.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;



@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid UserRegistration userRegistration) {
        try {
            InfoDto info = authService.register(userRegistration);
            return ResponseEntity.status(HttpStatus.CREATED).body(info);

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
    public ResponseEntity<?> forgotPassword(@RequestBody @Valid ForgotMyPassword email) {
        InfoDto info = authService.sendResetPasswordEmail(email);
        return ResponseEntity.status(HttpStatus.OK).body(info);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) throws Exception {
        InfoDto info = authService.resetPassword(resetPasswordRequest);
        return ResponseEntity.status(HttpStatus.OK).body(info);
    }


}
