package com.labndbnb.landbnb.service;

import com.labndbnb.landbnb.dto.aut_dto.UserRegistration;
import com.labndbnb.landbnb.dto.user_dto.UserDto;
import com.labndbnb.landbnb.dto.user_dto.UserUpdateDto;
import com.labndbnb.landbnb.mappers.UserRegistrationMapper;
import com.labndbnb.landbnb.model.User;
import com.labndbnb.landbnb.model.enums.UserStatuts;
import com.labndbnb.landbnb.repository.UserRepository;
import com.labndbnb.landbnb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRegistrationMapper userRegistrationMapper;
    private final UserRepository userRepository;


    private String encode (String password) {
        var passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    @Override
    public void create(UserRegistration userDto) throws Exception {

        if(existsByEmail(userDto.email())){
            throw new EmailAlreadyInUse("Email already in use");
        }

        User newuser = userRegistrationMapper.toEntity(userDto);
        newuser.setPassword(encode(newuser.getPassword()));
        userRepository.save(newuser);
    }

    private boolean existsByEmail(String email) {
        return userStore.values().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }


    @Override
    public UserDto get(String id) throws Exception {



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
