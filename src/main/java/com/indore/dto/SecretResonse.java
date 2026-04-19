package com.indore.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SecretResonse {
    private Long id;
    private String name;
    private String type;
    private LocalDateTime createdAt;
    private LocalDateTime expiryTime;
}
