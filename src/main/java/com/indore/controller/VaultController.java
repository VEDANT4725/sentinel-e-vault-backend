package com.indore.controller;


import com.indore.dto.CreateVaultRequest;
import com.indore.dto.SharedVaultResponse;
import com.indore.dto.VaultResponse;
import com.indore.entity.VaultAccess;
import com.indore.repository.VaultAccessRepository;
import com.indore.service.VaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vaults")
@RequiredArgsConstructor
public class VaultController {
    private final VaultService vaultService;
    private final VaultAccessRepository vaultAccessRepository;

    @PostMapping
    private String createdVault(@RequestBody CreateVaultRequest createVaultRequest, Authentication authentication){
        vaultService.createVault(createVaultRequest, authentication.getName());
        return "Vault created Successfully";
    }

    @GetMapping
    public List<VaultResponse> getVaults(Authentication authentication){
        return vaultService.getVaults(authentication.getName());
    }

    @GetMapping("/shared")
    public List<SharedVaultResponse> getSharedVaults(Authentication authentication){
        return vaultService.getSharedVaults(authentication.getName());
    }



}
