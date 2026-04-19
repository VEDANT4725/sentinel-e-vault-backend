package com.indore.controller;

import com.indore.dto.UpdatePasswordRequest;
import com.indore.dto.UpdateProfileRequest;
import com.indore.entity.User;
import com.indore.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final com.indore.service.FileService fileService;

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found")));
    }

    @PostMapping("/avatar")
    public ResponseEntity<String> uploadAvatar(Authentication authentication, @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        String fileName = fileService.uploadProfileImage(file);
        userService.updateProfileImage(authentication.getName(), fileName);
        return ResponseEntity.ok(fileName);
    }

    @DeleteMapping("/avatar")
    public ResponseEntity<String> deleteAvatar(Authentication authentication) {
        userService.updateProfileImage(authentication.getName(), null);
        return ResponseEntity.ok("Avatar removed");
    }

    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(Authentication authentication, @RequestBody UpdateProfileRequest request) {
        userService.updateProfile(authentication.getName(), request);
        return ResponseEntity.ok("Profile updated successfully");
    }

    @PutMapping("/password")
    public ResponseEntity<String> updatePassword(Authentication authentication, @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(authentication.getName(), request);
        return ResponseEntity.ok("Password updated successfully");
    }
}
