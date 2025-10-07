package com.labndbnb.landbnb.service;

import com.labndbnb.landbnb.dto.aut_dto.*;

public interface AuthService {

    Boolean register(UserRegistration request) throws Exception;

    AuthResponse login(LoginRequest request) throws Exception;

    void sendResetPasswordEmail(ForgotMyPassword email);

    void resetPassword(ResetPasswordRequest request);

    void changePassword(ChangePasswordRequest request);
}

