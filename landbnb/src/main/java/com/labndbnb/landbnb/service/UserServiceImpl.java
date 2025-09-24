package com.labndbnb.landbnb.service;

import com.labndbnb.landbnb.dto.aut_dto.UserRegistration;
import com.labndbnb.landbnb.dto.user_dto.UserDto;
import com.labndbnb.landbnb.dto.user_dto.UserUpdateDto;
import com.labndbnb.landbnb.mappers.UserRegistrationMapper;
import com.labndbnb.landbnb.model.User;
import com.labndbnb.landbnb.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserServiceImpl implements UserService {

    private final Map<String, User> userStore = new ConcurrentHashMap<>();
    private final UserRegistrationMapper userRegistrationMapper;


    public UserServiceImpl(UserRegistrationMapper userRegistrationMapper) {
        this.userRegistrationMapper = userRegistrationMapper;
    }

    private String encode (String password) {
        var passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    @Override
    public void create(UserRegistration userDto) throws Exception {

        if(existsByEmail(userDto.email())){
            throw new Exception("Email already in use");
        }

        //create user
        User user =  userRegistrationMapper.toEntity(userDto);

        User newUser = User.builder()
                .email(userDto.email())
                .name(userDto.name())
                .phoneNumber(userDto.phoneNumber())
                .role(userDto.userRole())
                .dateOfBirth(userDto.birthDate())
                .password(encode(userDto.password()))
                .createdAt(LocalDateTime.now())
                .build();

    }

    private boolean existsByEmail(String email) {
        return userStore.values().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }


    @Override
    public UserDto get(String id) throws Exception {

        User user = userStore.get(id);
        if (user == null) {
            throw new Exception("User not found");

        }
        //Mapeo
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getProfilePictureUrl(),
                user.getDateOfBirth()
        );

    }

    @Override
    public void delete(String id) throws Exception{

        User user = userStore.get(id);
        if (user == null) {
            throw new Exception("User not found");
        }
        userStore.remove(id);
    }

    @Override
    public List<UserDto> findAll() {
        return List.of();
    }

    @Override
    public void update(String id, UserUpdateDto userUpdateDto) throws Exception {

    }
}
