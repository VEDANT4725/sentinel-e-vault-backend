package com.indore.service;

import com.indore.dto.CreateVaultRequest;
import com.indore.dto.SharedVaultResponse;
import com.indore.dto.VaultResponse;

import java.util.List;

public interface VaultService {

    void createVault(CreateVaultRequest request, String email);
        List<VaultResponse>getVaults(String email);
        List<SharedVaultResponse> getSharedVaults(String userEmail);

}
