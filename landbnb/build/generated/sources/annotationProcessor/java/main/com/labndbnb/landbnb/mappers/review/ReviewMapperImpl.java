package com.labndbnb.landbnb.mappers.review;

import com.labndbnb.landbnb.dto.comment_dto.CommentDTO;
import com.labndbnb.landbnb.dto.user_dto.UserInfoDto;
import com.labndbnb.landbnb.model.Review;
import com.labndbnb.landbnb.model.User;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-11T16:08:43-0500",
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
        review.user( userInfoDtoToUser( dto.usuario() ) );

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
        UserInfoDto usuario = null;
        Integer id = null;

        calificacion = entity.getRating();
        texto = entity.getContent();
        fechaCreacion = entity.getCreatedAt();
        usuario = userToUserInfoDto( entity.getUser() );
        if ( entity.getId() != null ) {
            id = entity.getId().intValue();
        }

        String respuestaAnfitrion = mapReviewAnswerContent(entity.getReviewAnswer());

        CommentDTO commentDTO = new CommentDTO( id, calificacion, texto, respuestaAnfitrion, fechaCreacion, usuario );

        return commentDTO;
    }

    protected User userInfoDtoToUser(UserInfoDto userInfoDto) {
        if ( userInfoDto == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.name( userInfoDto.name() );
        user.lastName( userInfoDto.lastName() );

        return user.build();
    }

    protected UserInfoDto userToUserInfoDto(User user) {
        if ( user == null ) {
            return null;
        }

        String name = null;
        String lastName = null;

        name = user.getName();
        lastName = user.getLastName();

        String photoProfile = null;

        UserInfoDto userInfoDto = new UserInfoDto( name, lastName, photoProfile );

        return userInfoDto;
    }
}
