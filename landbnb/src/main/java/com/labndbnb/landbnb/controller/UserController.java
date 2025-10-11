package com.labndbnb.landbnb.controller;

import com.labndbnb.landbnb.dto.aut_dto.ChangePasswordRequest;
import com.labndbnb.landbnb.dto.user_dto.UserDto;
import com.labndbnb.landbnb.dto.user_dto.UserUpdateDto;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.service.definition.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProfile(HttpServletRequest request) throws Exception {
        UserDto userDto = userService.getUser(request);
        return ResponseEntity.status(HttpStatus.OK).body(userDto);
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProfile(@RequestBody @Valid UserUpdateDto userUpdateDto, HttpServletRequest request) throws Exception {
        InfoDto info = userService.update(userUpdateDto, request);
        return ResponseEntity.status(HttpStatus.OK).body(info);
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(
            @RequestBody @Valid  ChangePasswordRequest changePasswordRequest,
            HttpServletRequest request) {
        try {
            InfoDto info = userService.changePassword(changePasswordRequest, request);
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", true, "message", e.getMessage()));
        }
    }

    @PostMapping("/become-host")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> becomeHost(HttpServletRequest request) throws Exception {
        InfoDto info = userService.becomeHost(request);
        return ResponseEntity.status(HttpStatus.OK).body(info);
    }

    @DeleteMapping("/delete-account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteAccount(HttpServletRequest request) throws Exception {
        InfoDto info = userService.delete(request);
        return ResponseEntity.status(HttpStatus.OK).body(info);
    }
}
