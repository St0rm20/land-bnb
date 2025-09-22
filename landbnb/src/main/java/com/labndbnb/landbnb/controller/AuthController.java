package com.labndbnb.landbnb.controller;

import com.labndbnb.landbnb.dto.aut_dto.ChangePasswordRequest;
import com.labndbnb.landbnb.dto.aut_dto.LoginRequest;
import com.labndbnb.landbnb.dto.aut_dto.ResetPasswordRequest;
import com.labndbnb.landbnb.dto.aut_dto.UserRegistration;
import com.labndbnb.landbnb.service.AuthService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistration userRegistration) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
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
