package com.labndbnb.landbnb.service.definition;

import com.labndbnb.landbnb.exceptions.ExceptionAlert;

public interface ResetPasswordService {

    Boolean sendResetPasswordEmail(String email) throws ExceptionAlert;

    Boolean resetPassword(String email, String token, String newPassword) throws ExceptionAlert;
}
