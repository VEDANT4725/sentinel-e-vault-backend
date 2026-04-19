package com.indore.service;


import com.indore.dto.SessionResponse;

import java.util.List;

public interface SessionService {


    void createSession(String token, String device, String ip, String email);

    List<SessionResponse> getActiveSessions(String email, String currentToken);

    void revokeSession(Long id, String email);

    void revokeSessionByToken(String token);
}
