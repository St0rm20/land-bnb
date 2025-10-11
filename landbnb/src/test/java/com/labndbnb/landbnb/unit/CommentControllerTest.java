package com.labndbnb.landbnb.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labndbnb.landbnb.controller.CommentController;
import com.labndbnb.landbnb.dto.comment_dto.CommentAnswerDto;
import com.labndbnb.landbnb.dto.comment_dto.CommentDTO;
import com.labndbnb.landbnb.dto.comment_dto.ReviewRequest;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.service.definition.CommentService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private CommentController commentController;

    private ObjectMapper objectMapper;
    private ReviewRequest testReviewRequest;
    private CommentAnswerDto testCommentAnswerDto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        testReviewRequest = new ReviewRequest(1, 5, "Great accommodation!");
        testCommentAnswerDto = new CommentAnswerDto("Thank you for your feedback!", 1L);
    }

    @Test
    @DisplayName("Should create comment and return OK response")
    void shouldCreateComment_AndReturnOkResponse() throws ExceptionAlert {
        // Given
        InfoDto expectedInfo = new InfoDto("Review created", "Review created");
        when(commentService.createComment(any(ReviewRequest.class), any(HttpServletRequest.class)))
                .thenReturn(expectedInfo);

        // When
        ResponseEntity<?> response = commentController.createComment(testReviewRequest, httpServletRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedInfo);
        verify(commentService).createComment(testReviewRequest, httpServletRequest);
    }

    @Test
    @DisplayName("Should get accommodation comments with pagination")
    void shouldGetAccommodationComments_WithPagination() {
        // Given
        Integer accommodationId = 1;
        int page = 0;
        Page<CommentDTO> commentPage = new PageImpl<>(List.of(mock(CommentDTO.class)));
        when(commentService.getCommentsByAccommodation(accommodationId, page)).thenReturn(commentPage);

        // When
        ResponseEntity<?> response = commentController.getAccommodationsComments(accommodationId, page);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(commentPage);
        verify(commentService).getCommentsByAccommodation(accommodationId, page);
    }

    @Test
    @DisplayName("Should get comment by booking id")
    void shouldGetCommentByBookingId() throws ExceptionAlert {
        // Given
        Long bookingId = 1L;
        CommentDTO expectedComment = mock(CommentDTO.class);
        when(commentService.getCommentByBookingId(bookingId)).thenReturn(expectedComment);

        // When
        ResponseEntity<?> response = commentController.getCommentByBookingId(bookingId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedComment);
        verify(commentService).getCommentByBookingId(bookingId);
    }

    @Test
    @DisplayName("Should reply to comment and return OK response")
    void shouldReplyToComment_AndReturnOkResponse() throws ExceptionAlert {
        // Given
        CommentDTO expectedComment = mock(CommentDTO.class);
        when(commentService.replyToComment(any(CommentAnswerDto.class), any(HttpServletRequest.class)))
                .thenReturn(expectedComment);

        // When
        ResponseEntity<?> response = commentController.answerComment(testCommentAnswerDto, httpServletRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedComment);
        verify(commentService).replyToComment(testCommentAnswerDto, httpServletRequest);
    }

    @Test
    @DisplayName("Should delete comment and return OK response")
    void shouldDeleteComment_AndReturnOkResponse() throws ExceptionAlert {
        // Given
        Long commentId = 1L;
        InfoDto expectedInfo = new InfoDto("Review deleted", "Review deleted");
        when(commentService.deleteComment(commentId, httpServletRequest)).thenReturn(expectedInfo);

        // When
        ResponseEntity<?> response = commentController.deleteComment(commentId, httpServletRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedInfo);
        verify(commentService).deleteComment(commentId, httpServletRequest);
    }

    @Test
    @DisplayName("Should delete reply comment and return OK response")
    void shouldDeleteReplyComment_AndReturnOkResponse() throws ExceptionAlert {
        // Given
        Long replyId = 1L;
        InfoDto expectedInfo = new InfoDto("Reply deleted", "Reply deleted");
        when(commentService.deleteReplyComment(replyId, httpServletRequest)).thenReturn(expectedInfo);

        // When
        ResponseEntity<?> response = commentController.deleteReplyComment(replyId, httpServletRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedInfo);
        verify(commentService).deleteReplyComment(replyId, httpServletRequest);
    }
}