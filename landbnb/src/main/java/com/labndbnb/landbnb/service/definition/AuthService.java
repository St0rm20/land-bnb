package com.labndbnb.landbnb.service.definition;

import com.labndbnb.landbnb.dto.aut_dto.*;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;

public interface AuthService {

    InfoDto register(UserRegistration request) throws Exception;

    AuthResponse login(LoginRequest request) throws Exception;

    InfoDto sendResetPasswordEmail(ForgotMyPassword email);

    InfoDto resetPassword(ResetPasswordRequest request) throws Exception;

}

