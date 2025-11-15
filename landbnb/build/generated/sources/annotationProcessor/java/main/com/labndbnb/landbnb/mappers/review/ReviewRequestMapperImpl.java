package com.labndbnb.landbnb.mappers.review;

import com.labndbnb.landbnb.dto.comment_dto.ReviewRequest;
import com.labndbnb.landbnb.model.Booking;
import com.labndbnb.landbnb.model.Review;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-14T19:02:57-0500",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.3.jar, environment: Java 21.0.8 (BellSoft)"
)
@Component
public class ReviewRequestMapperImpl implements ReviewRequestMapper {

    @Override
    public Review toEntity(ReviewRequest dto) {
        if ( dto == null ) {
            return null;
        }

        Review.ReviewBuilder review = Review.builder();

        review.content( dto.text() );
        review.rating( dto.rating() );

        review.createdAt( java.time.LocalDateTime.now() );

        return review.build();
    }

    @Override
    public ReviewRequest toDto(Review entity) {
        if ( entity == null ) {
            return null;
        }

        Integer bookingId = null;
        Integer rating = null;
        String text = null;

        Long id = entityBookingId( entity );
        if ( id != null ) {
            bookingId = id.intValue();
        }
        rating = entity.getRating();
        text = entity.getContent();

        ReviewRequest reviewRequest = new ReviewRequest( bookingId, rating, text );

        return reviewRequest;
    }

    private Long entityBookingId(Review review) {
        if ( review == null ) {
            return null;
        }
        Booking booking = review.getBooking();
        if ( booking == null ) {
            return null;
        }
        Long id = booking.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
