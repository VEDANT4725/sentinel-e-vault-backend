package com.indore.controller;

import com.indore.dto.CreateSecretRequest;
import com.indore.dto.SecretResonse;
import com.indore.dto.UpdateSecretRequest;
import com.indore.dto.VerifyPasswordRequest;
import com.indore.service.SecretService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/secrets")
@RequiredArgsConstructor
public class SecretController {
    private final SecretService secretService;

    @PostMapping
    public void createSecret(@RequestBody CreateSecretRequest request, Authentication authentication){
        secretService.createSecret(request, authentication.getName());

    }

    @GetMapping("/{vaultId}")
    public List<SecretResonse> getSecrets(@PathVariable Long vaultId){
        return secretService.getSecretByVault(vaultId);
    }

    @PostMapping("/view/{id}")
    public  String viewSecret(@PathVariable Long id, @RequestBody VerifyPasswordRequest request, Authentication auth){
        return secretService.getDecryptedSecret(id, request.getPassword(), auth);
    }


    @DeleteMapping("/{id}")
    public String deleteSecret(@PathVariable Long id, Authentication authentication){
        secretService.deleteSecret(id, authentication.getName());
        return "Deleted";
    }

    @PutMapping("/{id}")
    public String updateSecret(@PathVariable Long id, @RequestBody UpdateSecretRequest request, Authentication authentication){
        secretService.updateSecret(id, request, authentication.getName());
        return "updated successfully";
    }
}
