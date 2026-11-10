package com.labndbnb.landbnb.service.definition;

import com.labndbnb.landbnb.dto.comment_dto.CommentAnswerDto;
import com.labndbnb.landbnb.dto.comment_dto.CommentDTO;
import com.labndbnb.landbnb.dto.comment_dto.ReviewRequest;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CommentService {

    InfoDto createComment(ReviewRequest reviewRequest, Long userId) throws ExceptionAlert;

    Page<CommentDTO> getCommentsByAccommodation(Integer alojamientoId, int page);

    CommentDTO replyToComment(CommentAnswerDto respuesta, Long userId) throws ExceptionAlert;

    CommentDTO getCommentByBookingId(Long bookingId, Long userId) throws ExceptionAlert;

    InfoDto deleteComment(Long id, Long userId) throws ExceptionAlert;

    InfoDto deleteReplyComment(Long id, Long userId) throws ExceptionAlert;

    boolean canUserCommentAccommodation(Long accommodationId, Long userId) throws ExceptionAlert;

    boolean canUserReplyComment(Long commentId, Long userId) throws ExceptionAlert;
}
