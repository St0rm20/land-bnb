package com.labndbnb.landbnb.service.definition;

import com.labndbnb.landbnb.dto.aut_dto.ChangePasswordRequest;
import com.labndbnb.landbnb.dto.aut_dto.UserRegistration;
import com.labndbnb.landbnb.dto.user_dto.UserDto;
import com.labndbnb.landbnb.dto.user_dto.UserUpdateDto;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.model.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface UserService {

    Boolean create(UserRegistration userDto) throws Exception;
    UserDto get(String id) throws  Exception;
    void delete(String id) throws Exception;
    List<UserDto> findAll();
    InfoDto update(UserUpdateDto userUpdateDto, HttpServletRequest request) throws Exception;
    UserDto getByEmail(String email) throws Exception;
    Boolean isThePasswordCorrect(String email, String password) throws Exception;
    InfoDto changePassword(ChangePasswordRequest changeRequest,  HttpServletRequest request);
    UserDto getUser(HttpServletRequest request) throws Exception;
    InfoDto becomeHost(HttpServletRequest request) throws Exception;
    InfoDto delete(HttpServletRequest request) throws Exception;
    User getUserFromRequest(HttpServletRequest request) throws Exception;

    void save(User user);
}
