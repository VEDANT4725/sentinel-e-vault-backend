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
@RequestMapping("api/auth")
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
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpServletRequest, HttpServletResponse response) {
        String token = authService.login(request, httpServletRequest);

        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("jwt_session", token);
        cookie.setHttpOnly(true);
        // Use secure true in production (requires HTTPS)
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // 1 day
        response.addCookie(cookie);

        return ResponseEntity.ok("Login Successful");
    }

    @GetMapping("/verify")
    public void verify(@RequestParam String token, jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        authService.verifyToken(token);
        String frontendUrl = System.getenv("FRONTEND_URL") != null ? System.getenv("FRONTEND_URL") : "http://localhost:5173";
        response.sendRedirect(frontendUrl + "/login?verified=true");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = null;
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("jwt_session".equals(cookie.getName())) {
                    token = cookie.getValue();
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                    break;
                }
            }
        }

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
