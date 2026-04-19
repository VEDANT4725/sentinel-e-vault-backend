package com.indore.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class InviteResponse {

    private Long id;
    private String vaultName;
    private String role;
    private String invitedBy;
    private String status;
    private String email;
    private List<String> secretNames;
}
