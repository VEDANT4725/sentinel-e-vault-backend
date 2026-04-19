package com.indore.service;

import com.indore.dto.LoginRequest;
import com.indore.dto.SignupRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    void signup(SignupRequest request);
    String login(LoginRequest request, HttpServletRequest httpServletRequest);

    void verifyToken(String token);
    void logout(String token);

    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
}
