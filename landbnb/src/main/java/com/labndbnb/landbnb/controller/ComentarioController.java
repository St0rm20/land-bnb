package com.labndbnb.landbnb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comentarios")
@RequiredArgsConstructor
public class ComentarioController {

    @PostMapping
    public ResponseEntity<Void> createComentario() {
        return ResponseEntity.status(201).build();
    }

    @GetMapping("/alojamiento/{alojamientoId}")
    public ResponseEntity<Void> getComentariosAlojamiento() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/responder")
    @PreAuthorize("hasRole('ANFITRION')")
    public ResponseEntity<Void> responderComentario() {
        return ResponseEntity.ok().build();
    }
}
