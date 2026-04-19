package com.indore.service.impl;

import com.indore.dto.LoginRequest;
import com.indore.dto.SignupRequest;
import com.indore.entity.Role;
import com.indore.entity.User;
import com.indore.entity.PasswordResetToken;
import com.indore.entity.VerificationToken;
import com.indore.repository.PasswordResetTokenRepository;
import com.indore.repository.RoleRepository;
import com.indore.repository.UserRepository;
import com.indore.repository.VerificationTokenRepository;
import com.indore.service.AuthService;
import com.indore.service.AuditLogService;
import com.indore.service.EmailService;
import com.indore.service.SessionService;
import com.indore.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final SessionService sessionService;
    private final AuditLogService auditLogService;

    public void signup(SignupRequest request){

//        1.validation
        if(!request.getPassword().equals(request.getConfirmPassword())){
            throw new RuntimeException("Passwords do not match ");
        }
        if(!request.isAcceptedTerms()){
            throw new RuntimeException("Please accept terms");
        }
        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email already exists");
        }

        //2.Get role
        Role role=roleRepository.findByName("ROLE_USER")
                .orElseThrow(() ->new RuntimeException("Role not found"));

        //3.user create krenge abh

        User user= User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .enabled(false)
                .build();

        userRepository.save(user);

//        4.create verification token
        String token= UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();
        verificationTokenRepository.save(verificationToken);

//        5.send Email
        emailService.sendVerificationEmail(user.getEmail(),token);

        auditLogService.log(user.getEmail(), "USER_SIGNUP", user.getName(), "New user registered", "SUCCESS", user.getEmail());
    }
    public String login(LoginRequest request, HttpServletRequest httpServletRequest){


        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            auditLogService.log(request.getEmail(), "LOGIN_FAILED", "Authentication", "Invalid credentials", "FAILED", request.getEmail());
            throw new RuntimeException("invalid credentials");
        }
        if(!user.isEnabled()){
            throw new RuntimeException("Please verify your email first");
        }

        // TODO: JWT generate karna hai Aage
        String token=jwtUtil.generateToken(user.getEmail());

        String device= httpServletRequest.getHeader("User-Agent");
        String ip= httpServletRequest.getRemoteAddr();

        sessionService.createSession(token,device, ip,user.getEmail());
        auditLogService.log(user.getEmail(), "LOGIN_SUCCESS", "Authentication", "User logged in from " + ip, "SUCCESS", user.getEmail());
        return token;

    }

    @Override
    public void verifyToken(String token) {

        VerificationToken verificationToken = verificationTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        // expiry check
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);

        userRepository.save(user);

        // optional: token delete kar de
        verificationTokenRepository.delete(verificationToken);
    }

    @Override
    public void logout(String token) {
        sessionService.revokeSessionByToken(token);
    }

    @Override
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with this email"));

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();
        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), token);
        auditLogService.log(user.getEmail(), "PASSWORD_RESET_REQUEST", "Authentication", "Password reset requested", "SUCCESS", user.getEmail());
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset link"));

        if (resetToken.isUsed()) {
            throw new RuntimeException("This reset link has already been used");
        }
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("This reset link has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        auditLogService.log(user.getEmail(), "PASSWORD_RESET_SUCCESS", "Authentication", "Password was reset", "SUCCESS", user.getEmail());
    }
}
