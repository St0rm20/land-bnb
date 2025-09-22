package com.labndbnb.landbnb.controller;

import com.labndbnb.landbnb.dto.comment_dto.CommentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comentarios")
public class CommentController {

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createComentario(@RequestBody CommentRequest comentarioRequest) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/alojamiento/{alojamientoId}")
    public ResponseEntity<?> getComentariosAlojamiento(
            @PathVariable Integer alojamientoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{id}/responder")
    @PreAuthorize("hasRole('ANFITRION')")
    public ResponseEntity<?> responderComentario(@PathVariable Integer id, @RequestParam String respuesta) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}

