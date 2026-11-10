package com.labndbnb.landbnb.controller;

import com.labndbnb.landbnb.dto.aut_dto.ChangePasswordRequest;
import com.labndbnb.landbnb.dto.user_dto.UserDto;
import com.labndbnb.landbnb.dto.user_dto.UserUpdateDto;
import com.labndbnb.landbnb.dto.util_dto.InfoDto;
import com.labndbnb.landbnb.exceptions.ExceptionAlert;
import com.labndbnb.landbnb.service.definition.UserService;
import com.labndbnb.landbnb.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal User currentUser) throws ExceptionAlert {
        UserDto userDto = userService.getUser(currentUser.getId());
        return ResponseEntity.status(HttpStatus.OK).body(userDto);
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProfile(@RequestBody @Valid UserUpdateDto userUpdateDto, @AuthenticationPrincipal User currentUser) throws ExceptionAlert {
        InfoDto info = userService.update(userUpdateDto, currentUser.getId());
        return ResponseEntity.status(HttpStatus.OK).body(info);
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(
            @RequestBody @Valid  ChangePasswordRequest changePasswordRequest,
            @AuthenticationPrincipal User currentUser) throws ExceptionAlert {
        InfoDto info = userService.changePassword(changePasswordRequest, currentUser.getId());
        return ResponseEntity.ok(info);
    }

    @PostMapping("/become-host")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> becomeHost(@AuthenticationPrincipal User currentUser) throws ExceptionAlert {
        InfoDto info = userService.becomeHost(currentUser.getId());
        return ResponseEntity.status(HttpStatus.OK).body(info);
    }

    @DeleteMapping("/delete-account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteAccount(@AuthenticationPrincipal User currentUser) throws ExceptionAlert {
        InfoDto info = userService.delete(currentUser.getId());
        return ResponseEntity.status(HttpStatus.OK).body(info);
    }
}
