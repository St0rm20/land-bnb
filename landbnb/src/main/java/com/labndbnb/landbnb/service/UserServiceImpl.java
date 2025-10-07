package com.labndbnb.landbnb.service;

import com.labndbnb.landbnb.dto.aut_dto.UserRegistration;
import com.labndbnb.landbnb.dto.user_dto.UserDto;
import com.labndbnb.landbnb.dto.user_dto.UserUpdateDto;
import com.labndbnb.landbnb.exceptions.EmailAlreadyInUse;
import com.labndbnb.landbnb.mappers.auth.UserRegistrationMapper;
import com.labndbnb.landbnb.mappers.user.UserDtoMapper;
import com.labndbnb.landbnb.model.User;
import com.labndbnb.landbnb.model.enums.UserRole;
import com.labndbnb.landbnb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRegistrationMapper userRegistrationMapper;
    private final UserDtoMapper userDtoMapper;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;



    private String encode (String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public Boolean create(UserRegistration userDto) throws Exception {
        if(existsByEmail(userDto.email())){
            throw new EmailAlreadyInUse("Email already in use");
        }

        User newUser = userRegistrationMapper.toEntity(userDto);
        newUser.setRole(UserRole.USER);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setPassword(encode(newUser.getPassword()));

       userRepository.save(newUser);
        return true;
    }

    private boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }


    @Override
    public UserDto get(String id) throws Exception {
        return userDtoMapper.toDto(userRepository
                .getUsersById(Integer.parseInt(id)));
    }

    @Override
    public void delete(String id) throws Exception {
        if (!userRepository.existsById(Integer.parseInt(id))) {
            throw new RuntimeException("User with id " + id + " not found");
        }
        userRepository.deleteById(Integer.parseInt(id));
    }
    @Override
    public List<UserDto> findAll() {
        return List.of();
    }

    @Override
    public void update(String id, UserUpdateDto userUpdateDto) throws Exception {
        if(!userRepository.existsById(Integer.parseInt(id))){
            throw new Exception("User with id " + id + " not found");
        }

        User user = userRepository.findById(Integer.parseInt(id)).get();
        user.setName(userUpdateDto.name());
        user.setPhoneNumber(userUpdateDto.phoneNumber());
        user.setBio(userUpdateDto.bio());
        user.setDateOfBirth(LocalDate.parse(userUpdateDto.dateBirth()));
        user.setProfilePictureUrl(userUpdateDto.photoProfile());
        userRepository.save(user);
    }

    @Override
    public UserDto getByEmail(String email) throws Exception {
        if(!userRepository.existsByEmail(email)){
            return null;
        }
        Optional<User> user = userRepository.findByEmail(email);

        return userDtoMapper.toDto(user.get());
    }

    @Override
    public Boolean isThePasswordCorrect(String email, String password) throws Exception {

        User user = userRepository.findByEmail(email).orElse(null);
        if(user == null){
            throw new Exception("User with email " + email + " not found");
        }
        System.out.println(passwordEncoder.matches(password, user.getPassword()));
        return passwordEncoder.matches(password, user.getPassword());

    }
}
