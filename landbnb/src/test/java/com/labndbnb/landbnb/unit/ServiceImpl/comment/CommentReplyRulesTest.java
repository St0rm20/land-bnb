package com.labndbnb.landbnb.unit.ServiceImpl.comment;

import com.labndbnb.landbnb.dto.comment_dto.CommentAnswerDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.mappers.review.ReviewMapper;
import com.labndbnb.landbnb.mappers.review.ReviewRequestMapper;
import com.labndbnb.landbnb.model.Accommodation;
import com.labndbnb.landbnb.model.Review;
import com.labndbnb.landbnb.model.ReviewAnswer;
import com.labndbnb.landbnb.model.User;
import com.labndbnb.landbnb.repository.AccommodationRepository;
import com.labndbnb.landbnb.repository.BookingRepository;
import com.labndbnb.landbnb.repository.ReviewAnswerRepository;
import com.labndbnb.landbnb.repository.ReviewRepository;
import com.labndbnb.landbnb.service.definition.BookingService;
import com.labndbnb.landbnb.service.definition.UserService;
import com.labndbnb.landbnb.service.implement.CommentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentServiceImpl - respuestas del host")
class CommentReplyRulesTest {

    @Mock private BookingService bookingService;
    @Mock private UserService userService;
    @Mock private ReviewRequestMapper reviewRequestMapper;
    @Mock private ReviewMapper reviewMapper;
    @Mock private ReviewRepository reviewRepository;
    @Mock private ReviewAnswerRepository reviewAnswerRepository;
    @Mock private AccommodationRepository accommodationRepository;
    @Mock private BookingRepository bookingRepository;

    @InjectMocks private CommentServiceImpl service;

    private User host;
    private Review review;

    @BeforeEach
    void setUp() {
        host = User.builder().id(1L).build();
        Accommodation acc = Accommodation.builder().id(10L).host(host).build();
        review = Review.builder().id(7L).accommodation(acc).rating(5).content("ok").build();
    }

    @Test
    @DisplayName("el host puede responder una reseña sin respuesta")
    void hostCanReplyOnce() throws ExceptionAlert {
        when(reviewRepository.findById(7L)).thenReturn(Optional.of(review));
        when(userService.getActiveUser(1L)).thenReturn(host);

        service.replyToComment(new CommentAnswerDto("gracias", 7L), 1L);

        verify(reviewAnswerRepository).save(any(ReviewAnswer.class));
        assertThat(review.getReviewAnswer()).isNotNull();
        assertThat(review.getReviewAnswer().getAnswer()).isEqualTo("gracias");
    }

    @Test
    @DisplayName("no se puede responder dos veces la misma reseña")
    void cannotReplyTwice() throws ExceptionAlert {
        review.setReviewAnswer(new ReviewAnswer());
        when(reviewRepository.findById(7L)).thenReturn(Optional.of(review));
        when(userService.getActiveUser(1L)).thenReturn(host);

        assertThatThrownBy(() -> service.replyToComment(new CommentAnswerDto("otra", 7L), 1L))
                .isInstanceOf(ExceptionAlert.class)
                .hasMessage("This review has already been answered");
        verify(reviewAnswerRepository, never()).save(any());
    }

    @Test
    @DisplayName("un host ajeno no puede responder")
    void otherHostCannotReply() throws ExceptionAlert {
        when(reviewRepository.findById(7L)).thenReturn(Optional.of(review));
        when(userService.getActiveUser(2L)).thenReturn(User.builder().id(2L).build());

        assertThatThrownBy(() -> service.replyToComment(new CommentAnswerDto("hola", 7L), 2L))
                .isInstanceOf(ExceptionAlert.class);
        verify(reviewAnswerRepository, never()).save(any());
    }

    @Test
    @DisplayName("canUserReplyComment es false si la reseña ya tiene respuesta")
    void canReplyFalseWhenAnswered() throws ExceptionAlert {
        review.setReviewAnswer(new ReviewAnswer());
        when(reviewRepository.findById(7L)).thenReturn(Optional.of(review));
        when(userService.getActiveUser(1L)).thenReturn(host);

        assertThat(service.canUserReplyComment(7L, 1L)).isFalse();
    }

    @Test
    @DisplayName("canUserReplyComment es true para el host de una reseña sin respuesta")
    void canReplyTrueForOwnerHost() throws ExceptionAlert {
        when(reviewRepository.findById(7L)).thenReturn(Optional.of(review));
        when(userService.getActiveUser(1L)).thenReturn(host);

        assertThat(service.canUserReplyComment(7L, 1L)).isTrue();
    }

    @Test
    @DisplayName("deleteReplyComment de un no-host lanza ExceptionAlert (antes devolvía 200)")
    void deleteReplyByNonHostThrows() throws ExceptionAlert {
        ReviewAnswer answer = new ReviewAnswer();
        answer.setReview(review);
        when(userService.getActiveUser(2L)).thenReturn(User.builder().id(2L).build());
        when(reviewAnswerRepository.findById(3L)).thenReturn(Optional.of(answer));

        assertThatThrownBy(() -> service.deleteReplyComment(3L, 2L))
                .isInstanceOf(ExceptionAlert.class);
        verify(reviewAnswerRepository, never()).delete(any());
    }
}
