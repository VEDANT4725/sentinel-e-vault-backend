package com.indore.service;

import com.indore.dto.InquiryRequest;

public interface InquiryService {
    void processAndSendInquiry(InquiryRequest request);
}
