package com.labndbnb.landbnb.service.definition;

import com.labndbnb.landbnb.dto.comment_dto.CommentAnswerDto;
import com.labndbnb.landbnb.dto.comment_dto.CommentDTO;
import com.labndbnb.landbnb.dto.comment_dto.ReviewRequest;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CommentService {

    InfoDto createComment(ReviewRequest reviewRequest, HttpServletRequest request) throws Exception;

    Page<CommentDTO> getCommentsByAccommodation(Integer alojamientoId, int page);

    CommentDTO replyToComment(CommentAnswerDto respuesta, HttpServletRequest request) throws Exception;

    CommentDTO getCommentByBookingId(Long bookingId) throws Exception;

    InfoDto deleteComment(Long id, HttpServletRequest request) throws Exception;

    InfoDto deleteReplyComment(Long id, HttpServletRequest request) throws Exception;
}
