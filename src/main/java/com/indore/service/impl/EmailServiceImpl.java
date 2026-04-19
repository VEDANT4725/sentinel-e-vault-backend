package com.indore.service.impl;

import com.indore.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Override
    public void sendVerificationEmail(String to, String token) {
        System.out.println("EMAIL METHOD CALL HO RHA");
        String backendUrl = System.getenv("BACKEND_URL") != null ? System.getenv("BACKEND_URL") : "http://localhost:8080";
        String link = backendUrl + "/api/auth/verify?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Verify your E-Vault account");
        message.setText("Click the link to verify your account:\n" + link);

        javaMailSender.send(message);
    }

    @Override
    public void sendInviteEmail(String to, String role, String vaultName, String invitedBy) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("vedantkolhe2005@gmail.com");
        message.setTo(to);
        message.setSubject("Vault Access Invite");
        String body = "Hello.\n\n" +
                "you have been invited by " +
                "role: " + role + "\n" +
                "VaultId" + vaultName + "\n" +
                "please login to accept the invite \n\n" +
                "Thanks, \nE-Vault";
        message.setText(body);
        javaMailSender.send(message);
    }

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        String frontendUrl = System.getenv("FRONTEND_URL") != null ? System.getenv("FRONTEND_URL") : "http://localhost:5173";
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Reset Your E-Vault Password");
            helper.setFrom("vedantkolhe2005@gmail.com");

            String html = """
                <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 520px; margin: 0 auto; background: #0a0a0a; border: 1px solid #27272a; border-radius: 16px; overflow: hidden;">
                    <div style="background: linear-gradient(135deg, #18181b 0%%, #0a0a0a 100%%); padding: 40px 32px 24px; text-align: center; border-bottom: 1px solid #27272a;">
                        <div style="display: inline-block; background: rgba(234,179,8,0.1); padding: 8px 16px; border-radius: 20px; border: 1px solid rgba(234,179,8,0.2); margin-bottom: 16px;">
                            <span style="color: #eab308; font-size: 10px; font-weight: 700; letter-spacing: 3px; text-transform: uppercase;">Password Reset</span>
                        </div>
                        <h1 style="color: #ffffff; font-size: 28px; font-weight: 800; margin: 16px 0 8px; letter-spacing: -0.5px;">Sentinel E-Vault</h1>
                        <p style="color: #71717a; font-size: 14px; margin: 0;">Secure Credential Recovery</p>
                    </div>
                    <div style="padding: 32px;">
                        <p style="color: #a1a1aa; font-size: 14px; line-height: 1.7; margin: 0 0 24px;">
                            We received a request to reset the password associated with your E-Vault account. Click the button below to create a new secure password.
                        </p>
                        <div style="text-align: center; margin: 32px 0;">
                            <a href="%s" style="display: inline-block; background: #eab308; color: #000000; padding: 14px 36px; border-radius: 8px; text-decoration: none; font-weight: 700; font-size: 14px; letter-spacing: 0.5px;">
                                Reset My Password
                            </a>
                        </div>
                        <p style="color: #52525b; font-size: 12px; line-height: 1.6; margin: 24px 0 0; padding-top: 20px; border-top: 1px solid #27272a;">
                            ⚠ This link will expire in <strong style="color: #a1a1aa;">15 minutes</strong>. If you did not request this reset, please ignore this email — your account remains fully secured.
                        </p>
                    </div>
                    <div style="background: #18181b; padding: 16px 32px; text-align: center; border-top: 1px solid #27272a;">
                        <p style="color: #3f3f46; font-size: 11px; margin: 0;">© 2026 Sentinel E-Vault · Zero-Knowledge Security</p>
                    </div>
                </div>
                """.formatted(resetLink);

            helper.setText(html, true);
            javaMailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
}
