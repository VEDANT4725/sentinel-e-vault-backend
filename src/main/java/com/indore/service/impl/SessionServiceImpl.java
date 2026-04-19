package com.indore.service.impl;

import com.indore.dto.SessionResponse;
import com.indore.entity.Session;
import com.indore.entity.User;
import com.indore.repository.SessionRepository;
import com.indore.repository.UserRepository;
import com.indore.service.AuditLogService;
import com.indore.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;


    @Override
    public void createSession(String token, String device, String ip, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Session session = Session.builder()
                .token(token)
                .device(device)
                .ipAddress(ip)
                .active(true)
                .loginTime(LocalDateTime.now())
                .user(user)
                .build();

        sessionRepository.save(session);
    }


    @Override
    public List<SessionResponse> getActiveSessions(String email, String currentToken) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return sessionRepository.findByUserAndActiveTrue(user)
                .stream()
                .map(session -> SessionResponse.builder()
                        .id(session.getId())
                        .device(session.getDevice())
                        .ipAddress(session.getIpAddress())
                        .loginTime(session.getLoginTime())
                        .currentSession(session.getToken().equals(currentToken))
                        .build()
                ).toList();
    }

    @Override
    public void revokeSession(Long id,String email) {

        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if(!session.getUser().getEmail().equals(email)){
            throw new RuntimeException("unauthorized");
        }
        session.setActive(false);
        sessionRepository.save(session);

        String sessionUserEmail = session.getUser().getEmail();
        auditLogService.log(sessionUserEmail, "SESSION_REVOKED", "Session", "Session manually revoked", "SUCCESS", sessionUserEmail);
    }


    @Override
    public void revokeSessionByToken(String token) {
        sessionRepository.findByToken(token).ifPresent(session -> {
            session.setActive(false);
            sessionRepository.save(session);
            String email = session.getUser().getEmail();
            auditLogService.log(email, "USER_LOGOUT", "Authentication", "User logged out successfully", "SUCCESS", email);
        });
    }
}