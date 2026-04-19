package com.indore.service;

import com.indore.dto.CreateSecretRequest;
import com.indore.dto.SecretResonse;
import com.indore.dto.UpdateSecretRequest;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface SecretService {
    void createSecret(CreateSecretRequest request, String userEmail );

    //For getting all data
    List<SecretResonse> getSecretByVault(Long vaultId);
    //for getDecrypted
    String getDecryptedSecret(Long id, String password, Authentication auth);
    //for deleting the key
    void deleteSecret(Long id, String userEmail);
    //for updating the key credential
    void updateSecret(Long id, UpdateSecretRequest request, String userEmail);



}
