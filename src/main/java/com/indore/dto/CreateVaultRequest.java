package com.indore.dto;

import lombok.Data;

@Data
public class CreateVaultRequest {

    private String name;
    private String description;
    private String encryptionType;
}
