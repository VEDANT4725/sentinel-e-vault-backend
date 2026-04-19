package com.indore.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SecretDto {
    private Long id;
    private String key;
    private String value;
    private String type;
}
