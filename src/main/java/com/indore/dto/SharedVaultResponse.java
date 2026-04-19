package com.indore.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SharedVaultResponse {
    private String vaultName;
    private String sharedBy;
    private String role;
    private List<SecretDto> secrets;
}
