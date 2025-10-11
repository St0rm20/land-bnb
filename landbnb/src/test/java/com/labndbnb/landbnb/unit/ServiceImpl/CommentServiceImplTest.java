package com.labndbnb.landbnb.unit.ServiceImpl;

import com.labndbnb.landbnb.dto.comment_dto.CommentAnswerDto;
import com.labndbnb.landbnb.dto.comment_dto.CommentDTO;
import com.labndbnb.landbnb.dto.comment_dto.ReviewRequest;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.mappers.review.ReviewMapper;
import com.labndbnb.landbnb.mappers.review.ReviewRequestMapper;
import com.labndbnb.landbnb.model.*;
import com.labndbnb.landbnb.repository.AccommodationRepository;
import com.labndbnb.landbnb.repository.ReviewAnswerRepository;
import com.labndbnb.landbnb.repository.ReviewRepository;
import com.labndbnb.landbnb.service.definition.BookingService;
import com.labndbnb.landbnb.service.definition.UserService;
import com.labndbnb.landbnb.service.implement.CommentServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private BookingService bookingService;

    @Mock
    private UserService userService;

    @Mock
    private ReviewRequestMapper reviewRequestMapper;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewAnswerRepository reviewAnswerRepository;

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User testUser;
    private Booking testBooking;
    private Accommodation testAccommodation;
    private Review testReview;
    private ReviewRequest testReviewRequest;

    @BeforeEach
    void setUp() {
        // Given - Common test data setup
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        testAccommodation = Accommodation.builder()
                .id(1L)
                .host(testUser)
                .averageRating(new BigDecimal("4.5"))
                .numberOfReviews(10)
                .build();

        testBooking = Booking.builder()
                .id(2L)
                .guest(testUser)
                .accommodation(testAccommodation)
                .endDate(LocalDateTime.now().minusDays(1)) // Past date
                .build();

        testReview = Review.builder()
                .id(1L)
                .rating(5)
                .content("Great place!")
                .user(testUser)
                .booking(testBooking)
                .accommodation(testAccommodation)
                .createdAt(LocalDateTime.now())
                .build();

        testReviewRequest = new ReviewRequest(2, 5, "Excellent accommodation!");
    }

    @Test
    @DisplayName("Should create comment when valid booking and user")
    void shouldCreateComment_WhenValidBookingAndUser() throws ExceptionAlert {
        // Given
        when(bookingService.getBookingById(2L)).thenReturn(testBooking);
        when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testUser);
        when(reviewRepository.existsByUser_IdAndBooking_Id(1L, 2L)).thenReturn(false);
        when(reviewRequestMapper.toEntity(testReviewRequest)).thenReturn(testReview);
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(accommodationRepository.save(any(Accommodation.class))).thenReturn(testAccommodation);

        // When
        InfoDto result = commentService.createComment(testReviewRequest, httpServletRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.message()).isEqualTo("Review created");
        verify(reviewRepository).save(any(Review.class));
        verify(accommodationRepository).save(testAccommodation);
    }

    @Test
    @DisplayName("Should throw exception when booking not found")
    void shouldThrowException_WhenBookingNotFound() throws ExceptionAlert {
        // Given
        when(bookingService.getBookingById(2L)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> commentService.createComment(testReviewRequest, httpServletRequest))
                .isInstanceOf(ExceptionAlert.class)
                .hasMessage("Booking not found");
    }

    @Test
    @DisplayName("Should throw exception when user not owner of booking")
    void shouldThrowException_WhenUserNotOwnerOfBooking() throws ExceptionAlert {
        // Given
        User differentUser = User.builder().id(99L).build();
        when(bookingService.getBookingById(2L)).thenReturn(testBooking);
        when(userService.getUserFromRequest(httpServletRequest)).thenReturn(differentUser);

        // When & Then
        assertThatThrownBy(() -> commentService.createComment(testReviewRequest, httpServletRequest))
                .isInstanceOf(ExceptionAlert.class)
                .hasMessage("User is not the owner of the booking");
    }

    @Test
    @DisplayName("Should throw exception when booking not completed")
    void shouldThrowException_WhenBookingNotCompleted() throws ExceptionAlert {
        // Given
        testBooking.setEndDate(LocalDateTime.now().plusDays(1)); // Future date
        when(bookingService.getBookingById(2L)).thenReturn(testBooking);
        when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testUser);

        // When & Then
        assertThatThrownBy(() -> commentService.createComment(testReviewRequest, httpServletRequest))
                .isInstanceOf(ExceptionAlert.class)
                .hasMessage("Booking is not completed");
    }

    @Test
    @DisplayName("Should throw exception when booking already reviewed")
    void shouldThrowException_WhenBookingAlreadyReviewed() throws ExceptionAlert {
        // Given
        when(bookingService.getBookingById(2L)).thenReturn(testBooking);
        when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testUser);
        when(reviewRepository.existsByUser_IdAndBooking_Id(1L, 2L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> commentService.createComment(testReviewRequest, httpServletRequest))
                .isInstanceOf(ExceptionAlert.class)
                .hasMessage("Booking already reviewed");
    }

    @Test
    @DisplayName("Should get comments by accommodation with pagination")
    void shouldGetCommentsByAccommodation_WithPagination() {
        // Given
        int page = 0;
        Long accommodationId = 1L;
        Pageable pageable = PageRequest.of(page, 10);
        Page<Review> reviewPage = new PageImpl<>(List.of(testReview));

        when(reviewRepository.getByAccommodationId(accommodationId, pageable)).thenReturn(reviewPage);
        when(reviewMapper.toDto(testReview)).thenReturn(mock(CommentDTO.class));

        // When
        Page<CommentDTO> result = commentService.getCommentsByAccommodation(accommodationId.intValue(), page);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(reviewRepository).getByAccommodationId(accommodationId, pageable);
    }

    @Test
    @DisplayName("Should reply to comment when user is host")
    void shouldReplyToComment_WhenUserIsHost() throws ExceptionAlert {
        // Given
        CommentAnswerDto answerDto = new CommentAnswerDto("Thank you for your feedback!", 1L);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testUser);
        when(reviewAnswerRepository.save(any(ReviewAnswer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(reviewMapper.toDto(testReview)).thenReturn(mock(CommentDTO.class));

        // When
        CommentDTO result = commentService.replyToComment(answerDto, httpServletRequest);

        // Then
        assertThat(result).isNotNull();
        verify(reviewAnswerRepository).save(any(ReviewAnswer.class));
        verify(reviewRepository).save(testReview);
    }

    @Test
    @DisplayName("Should throw exception when replying to non-existent comment")
    void shouldThrowException_WhenReplyingToNonExistentComment() {
        // Given
        CommentAnswerDto answerDto = new CommentAnswerDto("Thank you!", 99L);
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentService.replyToComment(answerDto, httpServletRequest))
                .isInstanceOf(ExceptionAlert.class)
                .hasMessage("Review not found");
    }

    @Test
    @DisplayName("Should get comment by booking id")
    void shouldGetCommentByBookingId() throws ExceptionAlert {
        // Given
        Long bookingId = 2L;
        when(reviewRepository.getByBookingId(bookingId)).thenReturn(testReview);
        when(reviewMapper.toDto(testReview)).thenReturn(mock(CommentDTO.class));

        // When
        CommentDTO result = commentService.getCommentByBookingId(bookingId);

        // Then
        assertThat(result).isNotNull();
        verify(reviewRepository).getByBookingId(bookingId);
    }

    @Test
    @DisplayName("Should throw exception when comment not found by booking id")
    void shouldThrowException_WhenCommentNotFoundByBookingId() {
        // Given
        Long bookingId = 99L;
        when(reviewRepository.getByBookingId(bookingId)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> commentService.getCommentByBookingId(bookingId))
                .isInstanceOf(ExceptionAlert.class)
                .hasMessage("Review not found");
    }

    @Test
    @DisplayName("Should delete comment when user is owner")
    void shouldDeleteComment_WhenUserIsOwner() throws ExceptionAlert {
        // Given
        Long commentId = 1L;
        when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testUser);
        when(reviewRepository.findById(commentId)).thenReturn(Optional.of(testReview));
        when(accommodationRepository.save(any(Accommodation.class))).thenReturn(testAccommodation);

        // When
        InfoDto result = commentService.deleteComment(commentId, httpServletRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.message()).isEqualTo("Review deleted");
        verify(reviewRepository).delete(testReview);
    }

    @Test
    @DisplayName("Should return info when comment not found for deletion")
    void shouldReturnInfo_WhenCommentNotFoundForDeletion() throws ExceptionAlert {
        // Given
        Long commentId = 99L;
        when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testUser);
        when(reviewRepository.findById(commentId)).thenReturn(Optional.empty());

        // When
        InfoDto result = commentService.deleteComment(commentId, httpServletRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.message()).isEqualTo("Review not found");
        verify(reviewRepository, never()).delete(any(Review.class));
    }

    @Test
    @DisplayName("Should delete reply comment when user is host")
    void shouldDeleteReplyComment_WhenUserIsHost() throws ExceptionAlert {
        // Given
        Long answerId = 1L;
        ReviewAnswer reviewAnswer = ReviewAnswer.builder()
                .id(answerId)
                .answer("Host reply")
                .review(testReview)
                .build();

        when(userService.getUserFromRequest(httpServletRequest)).thenReturn(testUser);
        when(reviewAnswerRepository.findById(answerId)).thenReturn(Optional.of(reviewAnswer));

        // When
        InfoDto result = commentService.deleteReplyComment(answerId, httpServletRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.message()).isEqualTo("Review answer deleted");
        verify(reviewAnswerRepository).delete(reviewAnswer);
    }
}