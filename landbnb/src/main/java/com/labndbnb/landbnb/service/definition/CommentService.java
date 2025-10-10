package com.labndbnb.landbnb.service.definition;

import com.labndbnb.landbnb.dto.comment_dto.CommentDTO;
import com.labndbnb.landbnb.dto.comment_dto.ReviewRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface CommentService {

    CommentDTO createComment(ReviewRequest reviewRequest, HttpServletRequest request);

    List<CommentDTO> getCommentsByAccommodation(Integer alojamientoId, int page, int size);

    CommentDTO replyToComment(Integer id, String respuesta, HttpServletRequest request);
}
