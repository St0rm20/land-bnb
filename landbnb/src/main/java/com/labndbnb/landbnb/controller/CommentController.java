package com.labndbnb.landbnb.controller;

import com.labndbnb.landbnb.dto.comment_dto.CommentAnswerDto;
import com.labndbnb.landbnb.dto.comment_dto.CommentDTO;
import com.labndbnb.landbnb.dto.comment_dto.ReviewRequest;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.service.definition.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final CommentService commentService;





    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createComment(@RequestBody ReviewRequest reviewRequest,
    HttpServletRequest requestServlet) throws ExceptionAlert {
        InfoDto info =  commentService.createComment(reviewRequest, requestServlet);
        return ResponseEntity.status(HttpStatus.OK).body(info);
    }

    @GetMapping("/accommodation/{AccommodationsId}")
    public ResponseEntity<?> getAccommodationsComments(
            @PathVariable Integer AccommodationsId,
            @RequestParam(defaultValue = "0")  int page){
        Page<CommentDTO>  commentDTOS= commentService.getCommentsByAccommodation(AccommodationsId, page);
        return ResponseEntity.status(HttpStatus.OK).body(commentDTOS);
    }


    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<?> getCommentByBookingId(@PathVariable Long bookingId) throws ExceptionAlert {
        CommentDTO commentDTO = commentService.getCommentByBookingId(bookingId);
        return ResponseEntity.status(HttpStatus.OK).body(commentDTO);
    }

    @PostMapping("/reply")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<?> answerComment(
            @RequestBody @Valid CommentAnswerDto answer,
            HttpServletRequest request) throws ExceptionAlert {
        CommentDTO commentDTO = commentService.replyToComment(answer, request);
        return ResponseEntity.status(HttpStatus.OK).body(commentDTO);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteComment(@PathVariable Long id, HttpServletRequest request) throws ExceptionAlert{
        InfoDto info = commentService.deleteComment(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(info);
    }


    @DeleteMapping("/host/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteReplyComment(
            @PathVariable Long id,
            HttpServletRequest request
    ) throws ExceptionAlert {

        InfoDto info = commentService.deleteReplyComment(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(info);
    }
}

