package com.indore.controller;

import com.indore.dto.InquiryRequest;
import com.indore.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping
    public ResponseEntity<String> submitInquiry(@RequestBody    InquiryRequest request) {
        inquiryService.processAndSendInquiry(request);
        return ResponseEntity.ok("Inquiry submitted successfully");
    }
}
