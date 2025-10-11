package com.labndbnb.landbnb.mappers.auth;

import com.labndbnb.landbnb.dto.aut_dto.UserRegistration;
import com.labndbnb.landbnb.model.User;
import com.labndbnb.landbnb.model.enums.UserStatus;
import java.time.LocalDate;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-11T13:42:30-0500",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.3.jar, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class UserRegistrationMapperImpl implements UserRegistrationMapper {

    @Override
    public User toEntity(UserRegistration dto) {
        if ( dto == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.password( dto.password() );
        user.phoneNumber( dto.phoneNumber() );
        user.dateOfBirth( dto.birthDate() );
        user.name( dto.name() );
        user.lastName( dto.lastName() );
        user.email( dto.email() );

        user.status( UserStatus.ACTIVE );
        user.emailVerified( false );

        return user.build();
    }

    @Override
    public UserRegistration toDto(User user) {
        if ( user == null ) {
            return null;
        }

        String phoneNumber = null;
        LocalDate birthDate = null;
        String email = null;
        String name = null;
        String lastName = null;

        phoneNumber = user.getPhoneNumber();
        birthDate = user.getDateOfBirth();
        email = user.getEmail();
        name = user.getName();
        lastName = user.getLastName();

        String password = null;

        UserRegistration userRegistration = new UserRegistration( email, password, name, lastName, phoneNumber, birthDate );

        return userRegistration;
    }
}
