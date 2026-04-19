package com.indore.dto;

import lombok.Data;

@Data
public class CreateSecretRequest {

    private String name;
    private String value;
    private String type;
    private Long vaultId;
    private Integer expiryDays;
}
