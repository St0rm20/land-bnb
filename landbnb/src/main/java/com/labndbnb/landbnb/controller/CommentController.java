package com.labndbnb.landbnb.controller;

import com.labndbnb.landbnb.dto.comment_dto.CommentAnswerDto;
import com.labndbnb.landbnb.dto.comment_dto.CommentDTO;
import com.labndbnb.landbnb.dto.comment_dto.ReviewRequest;
import com.labndbnb.landbnb.service.definition.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.xml.stream.events.Comment;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createComment(@RequestBody ReviewRequest reviewRequest,
    HttpServletRequest requestServlet) {

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/alojamiento/{alojamientoId}")
    public ResponseEntity<?> getAccommodationsComments(
            @PathVariable Integer AccommodationsId,
            @RequestParam(defaultValue = "0")  int page){
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{id}/responder")
    @PreAuthorize("hasRole('ANFITRION')")
    public ResponseEntity<?> answerComment(
            @PathVariable Long id,
            @RequestBody CommentAnswerDto answer,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}

