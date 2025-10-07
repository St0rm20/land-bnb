package com.labndbnb.landbnb.service;

import com.labndbnb.landbnb.dto.aut_dto.UserRegistration;
import com.labndbnb.landbnb.dto.user_dto.UserDto;
import com.labndbnb.landbnb.dto.user_dto.UserUpdateDto;
import com.labndbnb.landbnb.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    Boolean create(UserRegistration userDto) throws Exception;
    UserDto get(String id) throws  Exception;
    void delete(String id) throws Exception;
    List<UserDto> findAll();
    void update(String id, UserUpdateDto userUpdateDto) throws Exception;
    UserDto getByEmail(String email) throws Exception;
    Boolean isThePasswordCorrect(String email, String password) throws Exception;
}
