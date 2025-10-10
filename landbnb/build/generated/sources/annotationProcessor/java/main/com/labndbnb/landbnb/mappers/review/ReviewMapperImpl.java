package com.labndbnb.landbnb.mappers.review;

import com.labndbnb.landbnb.dto.comment_dto.CommentDTO;
import com.labndbnb.landbnb.dto.user_dto.UserDto;
import com.labndbnb.landbnb.model.Review;
import com.labndbnb.landbnb.model.User;
import com.labndbnb.landbnb.model.enums.UserRole;
import com.labndbnb.landbnb.model.enums.UserStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-10T18:00:35-0500",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.3.jar, environment: Java 21.0.8 (BellSoft)"
)
@Component
public class ReviewMapperImpl implements ReviewMapper {

    @Override
    public Review toEntity(CommentDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Review.ReviewBuilder review = Review.builder();

        if ( dto.id() != null ) {
            review.id( dto.id().longValue() );
        }
        review.rating( dto.calificacion() );
        review.content( dto.texto() );
        review.createdAt( dto.fechaCreacion() );
        review.user( userDtoToUser( dto.usuario() ) );

        review.reviewAnswer( mapToReviewAnswer(dto.respuestaAnfitrion()) );

        return review.build();
    }

    @Override
    public CommentDTO toDto(Review entity) {
        if ( entity == null ) {
            return null;
        }

        Integer calificacion = null;
        String texto = null;
        LocalDateTime fechaCreacion = null;
        UserDto usuario = null;
        Integer id = null;

        calificacion = entity.getRating();
        texto = entity.getContent();
        fechaCreacion = entity.getCreatedAt();
        usuario = userToUserDto( entity.getUser() );
        if ( entity.getId() != null ) {
            id = entity.getId().intValue();
        }

        String respuestaAnfitrion = mapReviewAnswerContent(entity.getReviewAnswer());

        CommentDTO commentDTO = new CommentDTO( id, calificacion, texto, respuestaAnfitrion, fechaCreacion, usuario );

        return commentDTO;
    }

    protected User userDtoToUser(UserDto userDto) {
        if ( userDto == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.id( userDto.id() );
        user.name( userDto.name() );
        user.lastName( userDto.lastName() );
        user.email( userDto.email() );
        user.phoneNumber( userDto.phoneNumber() );
        user.dateOfBirth( userDto.dateOfBirth() );
        user.profilePictureUrl( userDto.profilePictureUrl() );
        user.bio( userDto.bio() );

        return user.build();
    }

    protected UserDto userToUserDto(User user) {
        if ( user == null ) {
            return null;
        }

        Integer id = null;
        String email = null;
        String name = null;
        String lastName = null;
        String phoneNumber = null;
        String profilePictureUrl = null;
        LocalDate dateOfBirth = null;
        String bio = null;

        id = user.getId();
        email = user.getEmail();
        name = user.getName();
        lastName = user.getLastName();
        phoneNumber = user.getPhoneNumber();
        profilePictureUrl = user.getProfilePictureUrl();
        dateOfBirth = user.getDateOfBirth();
        bio = user.getBio();

        UserRole userRole = null;
        UserStatus userStatus = null;

        UserDto userDto = new UserDto( id, email, name, lastName, phoneNumber, userRole, profilePictureUrl, dateOfBirth, userStatus, bio );

        return userDto;
    }
}
