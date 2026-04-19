package com.indore.service;

public interface EmailService {
    void sendVerificationEmail(String to, String token);
    void sendInviteEmail(String to, String role, String vaultName, String invitedBy);
    void sendPasswordResetEmail(String to, String token);
}
