package com.labndbnb.landbnb.service.implement;

import com.labndbnb.landbnb.dto.comment_dto.CommentAnswerDto;
import com.labndbnb.landbnb.dto.comment_dto.CommentDTO;
import com.labndbnb.landbnb.dto.comment_dto.ReviewRequest;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.mappers.review.ReviewMapper;
import com.labndbnb.landbnb.mappers.review.ReviewRequestMapper;
import com.labndbnb.landbnb.model.Booking;
import com.labndbnb.landbnb.model.Review;
import com.labndbnb.landbnb.model.ReviewAnswer;
import com.labndbnb.landbnb.model.User;
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

import java.util.List;


@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    BookingService bookingService;
    UserService userService;
    ReviewRequestMapper reviewRequestMapper;
    ReviewMapper reviewMapper;
    ReviewRepository reviewRepository;
    ReviewAnswerRepository reviewAnswerRepository;
    private final int SIZE = 10;


    @Override
    public InfoDto createComment(ReviewRequest reviewRequest, HttpServletRequest request) throws Exception {
        Booking booking = bookingService.getBookingById(Long.valueOf(reviewRequest.bookingId()));
        User user = userService.getUserFromRequest(request);

        if(booking==null){
            throw new Exception("Booking not found");
        }
        if(user==null || !booking.getGuest().getId().equals(user.getId())){
            throw new Exception("User is not the guest of the booking");
        }

        if(isBookingReviewed(booking.getId(), user)){
            throw new Exception("Booking already reviewed");
        }

        Review review = reviewRequestMapper.toEntity(reviewRequest);
        review.setBooking(booking);
        review.setUser(user);
        review.setAccommodation(booking.getAccommodation());
        reviewRepository.save(review);
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
    public CommentDTO replyToComment(CommentAnswerDto answerDto, HttpServletRequest request) throws Exception {
        Review review = reviewRepository.findById(answerDto.commentId()).orElse(null);
        User user = userService.getUserFromRequest(request);
        if(review==null){
            throw new Exception("Review not found");
        }
        if(user==null || !review.getAccommodation().getHost().getId().equals(user.getId())){
            throw new Exception("User is not the host of the booking");
        }
        ReviewAnswer answer = new ReviewAnswer();
        answer.setAnswer(answer.getAnswer());
        answer.setReview(review);
        review.setReviewAnswer(answer);
        reviewRepository.save(review);
        reviewAnswerRepository.save(answer);
        return reviewMapper.toDto(review);
    }



    @Override
    public CommentDTO getCommentByBookingId(Long bookingId) throws Exception {
        Review review = reviewRepository.getByBookingId(bookingId);
        if(review==null){
            throw new Exception("Review not found");
        }
        return reviewMapper.toDto(review);
    }
}
