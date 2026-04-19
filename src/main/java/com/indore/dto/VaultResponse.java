package com.indore.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class VaultResponse {

    private Long id;
    private String name;
    private String description;
    private String status;
    private String vaultCode;
    private int secretsCount;
    private int membersCount;
    private LocalDateTime lastAccessTime;
}
