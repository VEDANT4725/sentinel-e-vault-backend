package com.indore.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateInviteRequest {

    private String email;
    private String role;
    private String scope;
    private Long vaultId;
    private List<Long> secretIds;
}
