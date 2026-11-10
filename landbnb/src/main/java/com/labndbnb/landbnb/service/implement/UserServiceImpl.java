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
import com.labndbnb.landbnb.model.enums.BookingStatus;
import com.labndbnb.landbnb.repository.AccommodationRepository;
import com.labndbnb.landbnb.repository.BookingRepository;
import com.labndbnb.landbnb.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import com.labndbnb.landbnb.service.definition.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRegistrationMapper userRegistrationMapper;
    private final UserDtoMapper userDtoMapper;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private static final long LOGIN_NOTIFICATION_MIN_HOURS = 24;

    @Value("${cloud.name:}")
    private String cloudName;

    private final BookingRepository bookingRepository;
    private final AccommodationRepository accommodationRepository;


    private String encode(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public Boolean create(UserRegistration userDto) throws ExceptionAlert {
        if (userRepository.existsByEmail(userDto.email())) {
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


    @Override
    public UserDto get(String id) throws ExceptionAlert {
        if (!userRepository.existsByIdAndStatus(Long.parseLong(id),UserStatus.ACTIVE)) {
            throw new ExceptionAlert("User with id " + id + " not found");
        }
        return userDtoMapper.toDto(userRepository
                .getUsersByIdAndStatus(Long.parseLong(id), UserStatus.ACTIVE));
    }

    @Override
    public List<UserDto> findAll() {
        return List.of();
    }

    @Override
    public InfoDto update(UserUpdateDto userUpdateDto, Long userId) throws ExceptionAlert {
        User user = getActiveUser(userId);
        user.setName(userUpdateDto.name());
        user.setLastName(userUpdateDto.lastName());
        user.setPhoneNumber(userUpdateDto.phoneNumber());
        if(user.getRole().equals(UserRole.HOST)){
            user.setBio(userUpdateDto.bio());
        }
        if(userUpdateDto.dateBirth() != null){
            try {
                user.setDateOfBirth(LocalDate.parse(userUpdateDto.dateBirth()));
            } catch (java.time.format.DateTimeParseException e) {
                throw new ExceptionAlert("Invalid date format, expected yyyy-MM-dd");
            }
        }
        user.setProfilePictureUrl(validatedPhotoUrl(userUpdateDto.photoProfile()));
        userRepository.save(user);
        return new InfoDto("Success update info", "Profile updated successfully");
    }

    /** La foto de perfil solo puede ser una imagen alojada en nuestro Cloudinary (o vacía para quitarla). */
    private String validatedPhotoUrl(String url) throws ExceptionAlert {
        if (url == null || url.isBlank()) {
            return null;
        }
        String prefix = "https://res.cloudinary.com/" + cloudName + "/";
        if (cloudName == null || cloudName.isBlank() || !url.startsWith(prefix)) {
            throw new ExceptionAlert("Profile picture must be an image uploaded through the platform");
        }
        return url;
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
        return passwordEncoder.matches(password, user.getPassword());

    }

    @Override
    public InfoDto changePassword(ChangePasswordRequest changeRequest, Long userId) throws ExceptionAlert {
        User user = getActiveUser(userId);

        if (!passwordEncoder.matches(changeRequest.currentPassword(), user.getPassword())) {
            throw new ExceptionAlert("Current password is incorrect");
        }

        user.setPassword(encode(changeRequest.newPassword()));
        user.setTokensValidAfter(LocalDateTime.now()); // invalida los JWT emitidos hasta ahora
        userRepository.save(user);

        return new InfoDto("Success", "Password changed successfully");
    }

    @Override
    public UserDto getUser(Long userId) throws ExceptionAlert {
        User user = getActiveUser(userId);
        return userDtoMapper.toDto(user);
    }

    @Override
    public InfoDto becomeHost(Long userId) throws ExceptionAlert {
        User user = getActiveUser(userId);
        if (user.getRole() == UserRole.HOST) {
            return new InfoDto("Info", "You are already a host");
        }
        user.setRole(UserRole.HOST);
        userRepository.save(user);
        return new InfoDto("Success", "You are now a host");
    }

    @Override
    @Transactional
    public InfoDto delete(Long userId) throws ExceptionAlert {
        User user = getActiveUser(userId);

        if (user.getRole() == UserRole.HOST) {
            boolean hasActiveBookings = bookingRepository
                    .existsByAccommodation_Host_IdAndEndDateAfterAndBookingStatusIn(
                            user.getId(), LocalDateTime.now(),
                            Set.of(BookingStatus.PENDING, BookingStatus.CONFIRMED));
            if (hasActiveBookings) {
                throw new ExceptionAlert("Cannot delete your account while your accommodations have active bookings");
            }
            // Sus alojamientos dejan de publicarse (borrado lógico, se conserva el historial)
            List<Accommodation> owned = accommodationRepository.findAllByHostIdAndActiveTrue(user.getId());
            owned.forEach(a -> a.setActive(false));
            accommodationRepository.saveAll(owned);
        }

        user.setStatus(UserStatus.INACTIVE);
        user.setTokensValidAfter(LocalDateTime.now());
        userRepository.save(user);
        return new InfoDto("Success", "Your account has been deleted");
    }

    @Override
    public User getActiveUser(Long userId) throws ExceptionAlert {
        if (userId == null) {
            throw new ExceptionAlert("User not found");
        }
        return userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new ExceptionAlert("User not found"));
    }

    @Override
    public boolean registerLogin(Long userId) throws ExceptionAlert {
        User user = getActiveUser(userId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime previous = user.getLastLoginAt();
        user.setLastLoginAt(now);
        userRepository.save(user);
        // Se avisa por correo como máximo una vez cada 24 h
        return previous == null || previous.isBefore(now.minusHours(LOGIN_NOTIFICATION_MIN_HOURS));
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
