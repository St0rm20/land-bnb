package com.labndbnb.landbnb.service.implement;

import com.labndbnb.landbnb.dto.comment_dto.CommentDTO;
import com.labndbnb.landbnb.dto.comment_dto.ReviewRequest;
import com.labndbnb.landbnb.service.definition.CommentService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public class CommentServiceImpl implements CommentService {
    @Override
    public CommentDTO createComment(ReviewRequest reviewRequest, HttpServletRequest request) {
        return null;
    }

    @Override
    public List<CommentDTO> getCommentsByAccommodation(Integer alojamientoId, int page, int size) {
        return List.of();
    }

    @Override
    public CommentDTO replyToComment(Integer id, String respuesta, HttpServletRequest request) {
        return null;
    }
}
