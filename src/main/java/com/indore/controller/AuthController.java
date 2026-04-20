package com.indore.controller;

import com.indore.dto.LoginRequest;
import com.indore.dto.SignupRequest;
import com.indore.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    //signup
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request){
        authService.signup(request);
        return ResponseEntity.ok("authService.signup(request);");
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, jakarta.servlet.http.HttpServletRequest httpServletRequest, jakarta.servlet.http.HttpServletResponse response) {
        String token = authService.login(request, httpServletRequest);

        org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie.from("jwt_session", token)
                .httpOnly(true)
                .secure(true) // Must be true for SameSite=None
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("None") // Required for cross-domain cookies (Vercel to Render)
                .build();

        response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString());

        // Return token in body too for devices that block cookies
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("token", token);
        body.put("message", "Login Successful");

        return ResponseEntity.ok(body);
    }

    @GetMapping("/verify")
    public void verify(@RequestParam String token, jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        authService.verifyToken(token);
        String frontendUrl = System.getenv("FRONTEND_URL") != null ? System.getenv("FRONTEND_URL") : "http://localhost:5173";
        response.sendRedirect(frontendUrl + "/login?verified=true");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) {
        String token = null;
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("jwt_session".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // Clear cookie using ResponseCookie
        org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie.from("jwt_session", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0) // Immediately expire
                .sameSite("None")
                .build();
        response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString());

        if (token != null) {
            authService.logout(token);
        }
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody java.util.Map<String, String> body) {
        String email = body.get("email");
        authService.forgotPassword(email);
        return ResponseEntity.ok("Password reset link sent to your email");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody java.util.Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        authService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Password reset successfully");
    }
}
