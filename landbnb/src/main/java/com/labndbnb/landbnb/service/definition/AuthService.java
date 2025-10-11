package com.labndbnb.landbnb.service.definition;

import com.labndbnb.landbnb.dto.aut_dto.*;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;

public interface AuthService {

    InfoDto register(UserRegistration request) throws ExceptionAlert;

    AuthResponse login(LoginRequest request) throws ExceptionAlert;

    InfoDto sendResetPasswordEmail(ForgotMyPassword email);

    InfoDto resetPassword(ResetPasswordRequest request) throws ExceptionAlert;

}

