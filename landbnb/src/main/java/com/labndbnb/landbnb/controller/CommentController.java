package com.labndbnb.landbnb.controller;

import com.labndbnb.landbnb.dto.comment_dto.CommentAnswerDto;
import com.labndbnb.landbnb.dto.comment_dto.CommentDTO;
import com.labndbnb.landbnb.dto.comment_dto.ReviewRequest;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.service.definition.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.xml.stream.events.Comment;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

CommentService commentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createComment(@RequestBody ReviewRequest reviewRequest,
    HttpServletRequest requestServlet) throws Exception {
        InfoDto info =  commentService.createComment(reviewRequest, requestServlet);
        return ResponseEntity.status(HttpStatus.OK).body(info);
    }

    @GetMapping("/alojamiento/{alojamientoId}")
    public ResponseEntity<?> getAccommodationsComments(
            @PathVariable Integer AccommodationsId,
            @RequestParam(defaultValue = "0")  int page){
        Page<CommentDTO>  commentDTOS= commentService.getCommentsByAccommodation(AccommodationsId, page);
        return ResponseEntity.status(HttpStatus.OK).body(commentDTOS);
    }


    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<?> getCommentByBookingId(@PathVariable Long bookingId) throws Exception {
        CommentDTO commentDTO = commentService.getCommentByBookingId(bookingId);
        return ResponseEntity.status(HttpStatus.OK).body(commentDTO);
    }

    @PostMapping("/{id}/responder")
    @PreAuthorize("hasRole('ANFITRION')")
    public ResponseEntity<?> answerComment(
            @RequestBody CommentAnswerDto answer,
            HttpServletRequest request) throws Exception {
        CommentDTO commentDTO = commentService.replyToComment(answer, request);
        return ResponseEntity.status(HttpStatus.OK).body(commentDTO);
    }
}

