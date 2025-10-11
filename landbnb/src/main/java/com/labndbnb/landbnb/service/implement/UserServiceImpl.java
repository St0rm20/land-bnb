package com.labndbnb.landbnb.service.implement;

import com.labndbnb.landbnb.dto.aut_dto.ChangePasswordRequest;
import com.labndbnb.landbnb.dto.aut_dto.UserRegistration;
import com.labndbnb.landbnb.dto.user_dto.UserDto;
import com.labndbnb.landbnb.dto.user_dto.UserUpdateDto;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.exceptions.EmailAlreadyInUse;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.mappers.auth.UserRegistrationMapper;
import com.labndbnb.landbnb.mappers.user.UserDtoMapper;
import com.labndbnb.landbnb.model.Accommodation;
import com.labndbnb.landbnb.model.User;
import com.labndbnb.landbnb.model.enums.UserRole;
import com.labndbnb.landbnb.model.enums.UserStatus;
import com.labndbnb.landbnb.repository.UserRepository;
import com.labndbnb.landbnb.security.JWTutils;
import com.labndbnb.landbnb.service.definition.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final JWTutils jwtutils;


    private String encode(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public Boolean create(UserRegistration userDto) throws ExceptionAlert {
        if (existsByEmail(userDto.email())) {
            throw new EmailAlreadyInUse("Email already in use");
        }

        User newUser = userRegistrationMapper.toEntity(userDto);
        newUser.setRole(UserRole.USER);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setPassword(encode(newUser.getPassword()));
        newUser.setStatus(UserStatus.ACTIVE);

        userRepository.save(newUser);
        return true;
    }

    private boolean existsByEmail(String email) {
        return userRepository.existsByEmailAndStatus(email, UserStatus.ACTIVE);
    }


    @Override
    public UserDto get(String id) throws ExceptionAlert {
        if (!userRepository.existsByIdAndStatus(Long.parseLong(id),UserStatus.ACTIVE)) {
            throw new ExceptionAlert("User with id " + id + " not found");
        }
        return userDtoMapper.toDto(userRepository
                .getUsersByIdAndStatus(Long.parseLong(id), UserStatus.ACTIVE));
    }

    @Override
    public void delete(String id) throws ExceptionAlert {
        if (!userRepository.existsByIdAndStatus(Long.parseLong(id), UserStatus.ACTIVE)) {
            throw new RuntimeException("User with id " + id + " not found");
        }
        userRepository.deleteById(Long.parseLong(id));
    }

    @Override
    public List<UserDto> findAll() {
        return List.of();
    }

    @Override
    public InfoDto update(UserUpdateDto userUpdateDto, HttpServletRequest request) throws ExceptionAlert {
        User user = getUserFromRequest(request);
        user.setName(userUpdateDto.name());
        user.setLastName(userUpdateDto.lastName());
        user.setPhoneNumber(userUpdateDto.phoneNumber());
        if(user.getRole().equals(UserRole.HOST)){
            user.setBio(userUpdateDto.bio());
        }
        user.setDateOfBirth(LocalDate.parse(userUpdateDto.dateBirth()));
        user.setProfilePictureUrl(userUpdateDto.photoProfile());
        userRepository.save(user);
        return new InfoDto("Success update info", "Profile updated successfully");
    }

    @Override
    public UserDto getByEmail(String email) throws ExceptionAlert {
        if (!userRepository.existsByEmailAndStatus(email, UserStatus.ACTIVE)) {
            return null;
        }
        Optional<User> user = userRepository.findByEmailAndStatus(email, UserStatus.ACTIVE);

        return userDtoMapper.toDto(user.get());
    }

    @Override
    public Boolean isThePasswordCorrect(String email, String password) throws ExceptionAlert {

        User user = userRepository.findByEmailAndStatus(email,UserStatus.ACTIVE).orElse(null);
        if (user == null) {
            throw new ExceptionAlert("User with email " + email + " not found");
        }
        System.out.println(passwordEncoder.matches(password, user.getPassword()));
        return passwordEncoder.matches(password, user.getPassword());

    }

    @Override
    public InfoDto changePassword(ChangePasswordRequest changeRequest, HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);

            if (!isThePasswordCorrect(user.getEmail(), changeRequest.currentPassword())) {
                return new InfoDto("Error", "Current password is incorrect");
            }

            user.setPassword(encode(changeRequest.newPassword()));
            userRepository.save(user);

            return new InfoDto("Success", "Password changed successfully");

        } catch (ExpiredJwtException e) {
            return new InfoDto("Error", "Token expired");
        } catch (JwtException e) {
            return new InfoDto("Error", "Invalid token");
        } catch (ExceptionAlert e) {
            return new InfoDto("Error", "Unexpected error: " + e.getMessage());
        }
    }

    @Override
    public UserDto getUser(HttpServletRequest request) throws ExceptionAlert {
        User user = getUserFromRequest(request);
        return userDtoMapper.toDto(user);
    }

    @Override
    public InfoDto becomeHost(HttpServletRequest request) throws ExceptionAlert {
        User user = getUserFromRequest(request);
        if (user.getRole() == UserRole.HOST) {
            return new InfoDto("Info", "You are already a host");
        }
        user.setRole(UserRole.HOST);
        userRepository.save(user);
        return new InfoDto("Success", "You are now a host");
    }

    @Override
    public InfoDto delete(HttpServletRequest request) throws ExceptionAlert {
        User user = getUserFromRequest(request);
        if (user.getRole() == UserRole.HOST) {
            return new InfoDto("Error", "Hosts cannot delete their accounts");
        }
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        return new InfoDto("Success", "Your account has been deleted");
    }

    @Override
    public User getUserFromRequest(HttpServletRequest request) throws ExceptionAlert {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ExceptionAlert("Missing or invalid Authorization header");
        }
        String token = header.substring(7);
        Jws<Claims> jws = jwtutils.parseJwt(token);

        String userId = jws.getPayload().get("userId", String.class);
        if (userId == null) {
            throw new ExceptionAlert("Invalid token: userId missing");
        }
        return userRepository.findByIdAndStatus(Long.parseLong(userId), UserStatus.ACTIVE)
                .orElseThrow(() -> new ExceptionAlert("User not found"));
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    @Override
    public Page<Accommodation> findFavoritesByUserId(Long userId, Pageable pageable) {
        return userRepository.findFavoritesByUserId(userId, pageable);
    }
}
