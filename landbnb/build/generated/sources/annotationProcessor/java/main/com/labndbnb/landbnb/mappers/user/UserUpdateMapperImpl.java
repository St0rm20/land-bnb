package com.labndbnb.landbnb.mappers.user;

import com.labndbnb.landbnb.dto.user_dto.UserUpdateDto;
import com.labndbnb.landbnb.model.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-11T16:08:43-0500",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.3.jar, environment: Java 21.0.8 (BellSoft)"
)
@Component
public class UserUpdateMapperImpl implements UserUpdateMapper {

    @Override
    public User toEntity(UserUpdateDto dto) {
        if ( dto == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.name( dto.name() );
        user.lastName( dto.lastName() );
        user.phoneNumber( dto.phoneNumber() );
        user.bio( dto.bio() );

        return user.build();
    }

    @Override
    public void updateUserFromDto(UserUpdateDto dto, User user) {
        if ( dto == null ) {
            return;
        }

        user.setProfilePictureUrl( dto.photoProfile() );
        user.setBio( dto.description() );
        user.setName( dto.name() );
        user.setLastName( dto.lastName() );
        user.setPhoneNumber( dto.phoneNumber() );
    }
}
