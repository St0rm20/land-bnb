package com.labndbnb.landbnb.service.implement;

import com.labndbnb.landbnb.dto.comment_dto.CommentAnswerDto;
import com.labndbnb.landbnb.dto.comment_dto.CommentDTO;
import com.labndbnb.landbnb.dto.comment_dto.ReviewRequest;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.mappers.review.ReviewMapper;
import com.labndbnb.landbnb.mappers.review.ReviewRequestMapper;
import com.labndbnb.landbnb.model.*;
import com.labndbnb.landbnb.model.enums.BookingStatus;
import com.labndbnb.landbnb.repository.AccommodationRepository;
import com.labndbnb.landbnb.repository.ReviewAnswerRepository;
import com.labndbnb.landbnb.repository.ReviewRepository;
import com.labndbnb.landbnb.service.definition.BookingService;
import com.labndbnb.landbnb.service.definition.CommentService;
import com.labndbnb.landbnb.service.definition.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor

public class CommentServiceImpl implements CommentService {

    private final BookingService bookingService;
    private final UserService userService;
    private final ReviewRequestMapper reviewRequestMapper;
    private final ReviewMapper reviewMapper;
    private final ReviewRepository reviewRepository;
    private final ReviewAnswerRepository reviewAnswerRepository;
    private final AccommodationRepository accommodationRepository;

    private final int SIZE = 10;


    @Override
    public InfoDto createComment(ReviewRequest reviewRequest, HttpServletRequest request) throws ExceptionAlert {
        Booking booking = bookingService.getBookingById(Long.valueOf(reviewRequest.bookingId()));
        User user = userService.getUserFromRequest(request);

        if(booking==null){
            throw new ExceptionAlert("Booking not found");
        }
        if(user==null){
            throw new ExceptionAlert("User is null");
        }
        if(!booking.getGuest().getId().equals(user.getId())){
            throw new ExceptionAlert("User is not the owner of the booking");
        }


        if(!booking.getBookingStatus().equals(BookingStatus.COMPLETED)){
            throw new ExceptionAlert("Booking is not completed");
        }

        if(isBookingReviewed(booking.getId(), user)){
            throw new ExceptionAlert("Booking already reviewed");
        }

        Review review = reviewRequestMapper.toEntity(reviewRequest);
        review.setBooking(booking);
        review.setUser(user);
        review.setAccommodation(booking.getAccommodation());
        reviewRepository.save(review);

        Accommodation accommodation = booking.getAccommodation();
        BigDecimal totalRating = accommodation.getAverageRating()
                .multiply(BigDecimal.valueOf(accommodation.getNumberOfReviews()));

        BigDecimal newRating = totalRating
                .add(BigDecimal.valueOf(reviewRequest.rating()))
                .divide(BigDecimal.valueOf(accommodation.getNumberOfReviews() + 1), 2, RoundingMode.HALF_UP);
        accommodation.setAverageRating(newRating);
        accommodation.setNumberOfReviews(accommodation.getNumberOfReviews() + 1);

        accommodationRepository.save(accommodation);
        return new InfoDto("Review created", "Review created");

    }

    private boolean isBookingReviewed(Long id, User user) {
        return reviewRepository.existsByUser_IdAndBooking_Id(user.getId(), id);
    }

    @Override
    public Page<CommentDTO> getCommentsByAccommodation(Integer alojamientoId, int page) {
        Pageable pageable = (Pageable) PageRequest.of(page, SIZE);
        Page<Review> reviews =  reviewRepository.getByAccommodationId(Long.valueOf(alojamientoId), pageable);
        return reviews.map(reviewMapper::toDto);
    }


    @Override
    public CommentDTO replyToComment(CommentAnswerDto answerDto, HttpServletRequest request) throws ExceptionAlert {
        Optional<Review> review = reviewRepository.findById(answerDto.commentId());
        if (review.isEmpty()) {
            throw new ExceptionAlert("Review not found");
        }
        User user = userService.getUserFromRequest(request);
        if (user == null || !review.get().getAccommodation().getHost().getId().equals(user.getId())) {
            throw new ExceptionAlert("User is not the host of the booking");
        }

        ReviewAnswer answer = new ReviewAnswer();
        answer.setAnswer(answerDto.message());
        answer.setReview(review.get());
        answer.setCreatedAt(LocalDate.now());

        reviewAnswerRepository.save(answer);
        review.get().setReviewAnswer(answer);
        reviewRepository.save(review.get());
        return reviewMapper.toDto(review.get());
    }




    @Override
    public CommentDTO getCommentByBookingId(Long bookingId) throws ExceptionAlert {
        Review review = reviewRepository.getByBookingId(bookingId);
        if(review==null){
            throw new ExceptionAlert("Review not found");
        }
        return reviewMapper.toDto(review);
    }

    @Override
    public InfoDto deleteComment(Long id, HttpServletRequest request) throws ExceptionAlert {
        User user = userService.getUserFromRequest(request);
        Review review = reviewRepository.findById(id).orElse(null);

        if (review == null) {
            return new InfoDto("Review not found", "Review not found");
        }

        if (user == null || review.getUser() == null || !review.getUser().getId().equals(user.getId())) {
            throw new ExceptionAlert("User is not the owner of the review");
        }

        Accommodation accommodation = review.getAccommodation();
        if (accommodation != null) {
            int currentReviews = accommodation.getNumberOfReviews();

            if (currentReviews <= 1) {
                accommodation.setAverageRating(BigDecimal.ZERO);
                accommodation.setNumberOfReviews(0);
            } else {
                BigDecimal totalRating = accommodation.getAverageRating()
                        .multiply(BigDecimal.valueOf(currentReviews));

                BigDecimal newTotal = totalRating.subtract(BigDecimal.valueOf(review.getRating()));

                BigDecimal newAverage = newTotal.divide(
                        BigDecimal.valueOf(currentReviews - 1), 2, RoundingMode.HALF_UP
                );

                accommodation.setAverageRating(newAverage);
                accommodation.setNumberOfReviews(currentReviews - 1);
            }

            accommodationRepository.save(accommodation);
        }

        reviewRepository.delete(review);
        return new InfoDto("Review deleted", "Review deleted");
    }


    @Override
    public InfoDto deleteReplyComment(Long id, HttpServletRequest request) throws ExceptionAlert {
        User user = userService.getUserFromRequest(request);
        ReviewAnswer reviewAnswer = reviewAnswerRepository.findById(id).orElse(null);

        if (reviewAnswer == null) {
            return new InfoDto("Review answer not found", "Review answer not found");
        }

        if (user == null || !reviewAnswer.getReview().getAccommodation().getHost().getId().equals(user.getId())) {
            return new InfoDto("User is not the host of the accommodation", "User is not the host of the accommodation");
        }

        Review review = reviewAnswer.getReview();
        review.setReviewAnswer(null);
        reviewRepository.save(review);

        reviewAnswerRepository.delete(reviewAnswer);

        return new InfoDto("Review answer deleted", "Review answer deleted");
    }

}
