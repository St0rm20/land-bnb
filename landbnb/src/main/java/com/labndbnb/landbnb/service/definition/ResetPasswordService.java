package com.labndbnb.landbnb.service.definition;

public interface ResetPasswordService {

    Boolean sendResetPasswordEmail(String email) throws Exception;

    Boolean resetPassword(String email, String token, String newPassword) throws Exception;
}
