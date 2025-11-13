package com.labndbnb.landbnb.mappers.user;

import com.labndbnb.landbnb.dto.user_dto.UserDto;
import com.labndbnb.landbnb.model.User;
import com.labndbnb.landbnb.model.enums.UserRole;
import com.labndbnb.landbnb.model.enums.UserStatus;
import java.time.LocalDate;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-12T19:16:06-0500",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.3.jar, environment: Java 21.0.8 (BellSoft)"
)
@Component
public class UserDtoMapperImpl implements UserDtoMapper {

    @Override
    public UserDto toDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserRole userRole = null;
        UserStatus userStatus = null;
        Integer id = null;
        String email = null;
        String name = null;
        String lastName = null;
        String phoneNumber = null;
        String profilePictureUrl = null;
        LocalDate dateOfBirth = null;
        String bio = null;

        userRole = user.getRole();
        userStatus = user.getStatus();
        if ( user.getId() != null ) {
            id = user.getId().intValue();
        }
        email = user.getEmail();
        name = user.getName();
        lastName = user.getLastName();
        phoneNumber = user.getPhoneNumber();
        profilePictureUrl = user.getProfilePictureUrl();
        dateOfBirth = user.getDateOfBirth();
        bio = user.getBio();

        UserDto userDto = new UserDto( id, email, name, lastName, phoneNumber, userRole, profilePictureUrl, dateOfBirth, userStatus, bio );

        return userDto;
    }

    @Override
    public User toEntity(UserDto dto) {
        if ( dto == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.role( dto.userRole() );
        user.status( dto.userStatus() );
        user.bio( dto.bio() );
        if ( dto.id() != null ) {
            user.id( dto.id().longValue() );
        }
        user.name( dto.name() );
        user.lastName( dto.lastName() );
        user.email( dto.email() );
        user.phoneNumber( dto.phoneNumber() );
        user.dateOfBirth( dto.dateOfBirth() );
        user.profilePictureUrl( dto.profilePictureUrl() );

        return user.build();
    }
}
