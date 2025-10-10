package com.labndbnb.landbnb.mappers.review;

import com.labndbnb.landbnb.dto.comment_dto.CommentDTO;
import com.labndbnb.landbnb.model.Review;
import com.labndbnb.landbnb.model.ReviewAnswer;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReviewMapper {


    @Mapping(target = "id", source = "id")
    @Mapping(target = "rating", source = "calificacion")
    @Mapping(target = "content", source = "texto")
    @Mapping(target = "createdAt", source = "fechaCreacion")
    @Mapping(target = "user", source = "usuario")
    @Mapping(target = "booking", ignore = true)
    @Mapping(target = "accommodation", ignore = true)
    @Mapping(target = "reviewAnswer", expression = "java(mapToReviewAnswer(dto.respuestaAnfitrion()))")
    Review toEntity(CommentDTO dto);


    @Mapping(target = "calificacion", source = "rating")
    @Mapping(target = "texto", source = "content")

    @Mapping(target = "respuestaAnfitrion", expression = "java(mapReviewAnswerContent(entity.getReviewAnswer()))")
    @Mapping(target = "fechaCreacion", source = "createdAt")
    @Mapping(target = "usuario", source = "user")
    CommentDTO toDto(Review entity);


    default String mapReviewAnswerContent(ReviewAnswer reviewAnswer) {
        return reviewAnswer != null ? reviewAnswer.getAnswer() : null;
    }

    default ReviewAnswer mapToReviewAnswer(String respuestaAnfitrion) {
        if (respuestaAnfitrion == null || respuestaAnfitrion.trim().isEmpty()) {
            return null;
        }
        ReviewAnswer answer = new ReviewAnswer();
        answer.setAnswer(respuestaAnfitrion);
        return answer;
    }
}
