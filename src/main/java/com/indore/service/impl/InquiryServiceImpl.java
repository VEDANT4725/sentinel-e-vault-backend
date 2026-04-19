package com.indore.service.impl;

import com.indore.dto.InquiryRequest;
import com.indore.entity.Inquiry;
import com.indore.repository.InquiryRepository;
import com.indore.service.InquiryService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InquiryServiceImpl implements InquiryService {

    private final InquiryRepository inquiryRepository;
    private final JavaMailSender mailSender;

    @Override
    public void processAndSendInquiry(InquiryRequest request) {
        // 1. Map to Entity
        Inquiry inquiry = Inquiry.builder()
                .name(request.getName())
                .email(request.getEmail())
                .company(request.getCompany())
                .inquiryType(request.getInquiryType())
                .message(request.getMessage())
                .createdAt(LocalDateTime.now())
                .build();

        // 2. Save to DB
        inquiryRepository.save(inquiry);

        // 3. Send HTML Email
        sendHtmlEmailNotification(inquiry);
    }

    private void sendHtmlEmailNotification(Inquiry inquiry) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // Pass 'true' for multipart (in case you add attachments later)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo("vedantkolhe2005@gmail.com");
            helper.setSubject("🔥 New Talk To Us Inquiry: " + inquiry.getInquiryType().toUpperCase());

            // Build an awesome HTML string with inline CSS
            String company = inquiry.getCompany() != null && !inquiry.getCompany().isBlank()
                    ? inquiry.getCompany() : "N/A";

            String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #333; border-radius: 8px; overflow: hidden;'>"
                    + "<div style='background-color: #000; padding: 20px; text-align: center; color: #fff;'>"
                    + "<h2 style='margin: 0; letter-spacing: 2px; color: #FFD60A;'>SENTINEL E-VAULT</h2>"
                    + "<p style='margin: 5px 0 0 0; font-size: 14px; color: #888;'>New Talk To Us Submission</p>"
                    + "</div>"
                    + "<div style='padding: 30px; background-color: #f9f9f9;'>"
                    + "<h3 style='color: #333; border-bottom: 2px solid #ddd; padding-bottom: 10px;'>Inquiry Details</h3>"
                    + "<p><strong>Name:</strong> " + inquiry.getName() + "</p>"
                    + "<p><strong>Email:</strong> <a href='mailto:" + inquiry.getEmail() + "' style='color: #4F95FF;'>" + inquiry.getEmail() + "</a></p>"
                    + "<p><strong>Company:</strong> " + company + "</p>"
                    + "<p><strong>Type:</strong> <span style='background-color: #333; color: #fff; padding: 3px 8px; border-radius: 4px; font-size: 12px;'>" + inquiry.getInquiryType().toUpperCase() + "</span></p>"
                    + "<div style='margin-top: 25px; padding: 15px; background-color: #fff; border-left: 4px solid #4F95FF; border-radius: 4px; box-shadow: 0 2px 5px rgba(0,0,0,0.05);'>"
                    + "<h4 style='margin-top: 0; color: #555;'>Message:</h4>"
                    + "<p style='color: #444; line-height: 1.5;'>" + inquiry.getMessage().replace("\n", "<br>") + "</p>"
                    + "</div>"
                    + "</div>"
                    + "<div style='background-color: #111; color: #666; text-align: center; padding: 15px; font-size: 12px;'>"
                    + "Submitted on " + inquiry.getCreatedAt().toString()
                    + "</div>"
                    + "</div>";

            // Set to "true" to send as HTML
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send HTML inquiry email: " + e.getMessage());
        }
    }
}
