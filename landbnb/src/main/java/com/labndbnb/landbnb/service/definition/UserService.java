package com.labndbnb.landbnb.service.definition;

import com.labndbnb.landbnb.dto.aut_dto.ChangePasswordRequest;
import com.labndbnb.landbnb.dto.aut_dto.UserRegistration;
import com.labndbnb.landbnb.dto.user_dto.UserDto;
import com.labndbnb.landbnb.dto.user_dto.UserUpdateDto;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.model.Accommodation;
import com.labndbnb.landbnb.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    Boolean create(UserRegistration userDto) throws ExceptionAlert;
    UserDto get(String id) throws  ExceptionAlert;
    List<UserDto> findAll();
    InfoDto update(UserUpdateDto userUpdateDto, Long userId) throws ExceptionAlert;
    UserDto getByEmail(String email) throws ExceptionAlert;
    Boolean isThePasswordCorrect(String email, String password) throws ExceptionAlert;
    InfoDto changePassword(ChangePasswordRequest changeRequest, Long userId) throws ExceptionAlert;
    UserDto getUser(Long userId) throws ExceptionAlert;
    InfoDto becomeHost(Long userId) throws ExceptionAlert;
    InfoDto delete(Long userId) throws ExceptionAlert;
    User getActiveUser(Long userId) throws ExceptionAlert;

    /** Registra un inicio de sesión exitoso. Devuelve true si corresponde avisar al usuario por correo. */
    boolean registerLogin(Long userId) throws ExceptionAlert;

    void save(User user);

    Page<Accommodation> findFavoritesByUserId(Long userId, Pageable pageable);

}
