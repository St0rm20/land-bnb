package com.labndbnb.landbnb.mappers.review;
import com.labndbnb.landbnb.dto.comment_dto.ReviewRequest;
import com.labndbnb.landbnb.model.Review;
import org.mapstruct.*;



@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReviewRequestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "content", source = "text")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "booking", ignore = true)
    @Mapping(target = "accommodation", ignore = true)
    @Mapping(target = "reviewAnswer", ignore = true)
    Review toEntity(ReviewRequest dto);

    @Mapping(target = "bookingId", source = "booking.id")
    @Mapping(target = "rating", source = "rating")
    @Mapping(target = "text", source = "content")
    ReviewRequest toDto(Review entity);
}
