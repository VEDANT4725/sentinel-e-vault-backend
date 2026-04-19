package com.indore.dto;

import lombok.Data;

@Data
public class UpdateSecretRequest {

    private String name;
    private String value;
    private String type;
    private Integer expiryDays;
}
