package com.indore.controller;

import com.indore.dto.SessionResponse;
import com.indore.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping("/sessions")
    public List<SessionResponse> getSession(Authentication auth, jakarta.servlet.http.HttpServletRequest request){
        String email = auth.getName();
        String token = null;

        // 1. Try to get from Cookie
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("jwt_session".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // 2. Fallback to Authorization Header
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        return sessionService.getActiveSessions(email, token);
    }

    @DeleteMapping("/sessions/{id}")
    public String revokeSession(@PathVariable Long id, Authentication authentication){
        String email= authentication.getName();
        sessionService.revokeSession(id, email);
        return "session revoked successfully";
    }

}
