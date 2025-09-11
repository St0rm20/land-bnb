package com.labndbnb.landbnb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/profile")
    public ResponseEntity<Void> getProfile() {
        return ResponseEntity.ok().build();
    }

    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile() {
        return ResponseEntity.ok().build();
    }
}
