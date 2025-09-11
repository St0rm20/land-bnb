package com.labndbnb.landbnb.service;

import com.labndbnb.landbnb.dto.*;

public interface AuthService {

    AuthResponse register(UserRegistration request);

    AuthResponse login(LoginRequest request);

    void sendResetPasswordEmail(String email);

    void resetPassword(ResetPasswordRequest request);

    void changePassword(ChangePasswordRequest request);
}

