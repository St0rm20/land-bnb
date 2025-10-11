package com.labndbnb.landbnb.service.definition;

import com.labndbnb.landbnb.dto.aut_dto.ChangePasswordRequest;
import com.labndbnb.landbnb.dto.aut_dto.UserRegistration;
import com.labndbnb.landbnb.dto.user_dto.UserDto;
import com.labndbnb.landbnb.dto.user_dto.UserUpdateDto;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.model.Accommodation;
import com.labndbnb.landbnb.model.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserService {

    Boolean create(UserRegistration userDto) throws ExceptionAlert;
    UserDto get(String id) throws  ExceptionAlert;
    void delete(String id) throws ExceptionAlert;
    List<UserDto> findAll();
    InfoDto update(UserUpdateDto userUpdateDto, HttpServletRequest request) throws ExceptionAlert;
    UserDto getByEmail(String email) throws ExceptionAlert;
    Boolean isThePasswordCorrect(String email, String password) throws ExceptionAlert;
    InfoDto changePassword(ChangePasswordRequest changeRequest,  HttpServletRequest request);
    UserDto getUser(HttpServletRequest request) throws ExceptionAlert;
    InfoDto becomeHost(HttpServletRequest request) throws ExceptionAlert;
    InfoDto delete(HttpServletRequest request) throws ExceptionAlert;
    User getUserFromRequest(HttpServletRequest request) throws ExceptionAlert;

    void save(User user);

    Page<Accommodation> findFavoritesByUserId(Long userId, Pageable pageable);

}
