package com.indore.dto;

import lombok.Data;

@Data
public class InquiryRequest {
    private String name;
    private String email;
    private String company;
    private String inquiryType;
    private String message;
}
