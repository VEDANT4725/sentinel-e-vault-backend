package com.indore.service.impl;

import com.indore.dto.CreateSecretRequest;
import com.indore.dto.SecretResonse;
import com.indore.dto.UpdateSecretRequest;
import com.indore.entity.Secret;
import com.indore.entity.User;
import com.indore.entity.Vault;
import com.indore.entity.VaultAccess;
import com.indore.repository.SecretRepository;
import com.indore.repository.UserRepository;
import com.indore.repository.VaultAccessRepository;
import com.indore.repository.VaultRepository;
import com.indore.service.AuditLogService;
import com.indore.service.SecretService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SecretServiceImpl implements SecretService {

    private final SecretRepository secretRepository;
    private final VaultRepository vaultRepository;
    private final VaultAccessRepository vaultAccessRepository;
    private final AuditLogService auditLogService;
    private final EncryptionService encryptionService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.indore.repository.InviteRepository inviteRepository;

    //  EDIT CHECK (EDITOR + OWNER for specific secret)
    private void checkSecretEditAccess(String userEmail, Secret secret){
        Vault vault = secret.getVault();

        // OWNER allowed
        if(vault.getOwner().getEmail().equals(userEmail)){
            return;
        }

        // Check if user has an ACCEPTED invite for THIS specific secret with EDITOR role
        List<com.indore.entity.Invite> accepted = inviteRepository.findByEmailAndVaultIdAndStatus(userEmail, vault.getId(), "ACCEPTED");
        boolean hasEditorAccess = accepted.stream()
                .filter(inv -> "EDITOR".equalsIgnoreCase(inv.getRole()))
                .filter(inv -> inv.getSecretIds() != null)
                .flatMap(inv -> inv.getSecretIds().stream())
                .anyMatch(id -> id.equals(secret.getId()));

        if (!hasEditorAccess) {
            throw new RuntimeException("You do not have EDITOR access to this specific secret");
        }
    }

    //  VIEW CHECK (VIEWER + EDITOR + OWNER for specific secret)
    private void checkSecretViewAccess(String userEmail, Secret secret){
        Vault vault = secret.getVault();

        // OWNER allowed
        if(vault.getOwner().getEmail().equals(userEmail)){
            return;
        }

        // Check if user has an ACCEPTED invite for THIS specific secret
        List<com.indore.entity.Invite> accepted = inviteRepository.findByEmailAndVaultIdAndStatus(userEmail, vault.getId(), "ACCEPTED");
        boolean hasAccess = accepted.stream()
                .filter(inv -> inv.getSecretIds() != null)
                .flatMap(inv -> inv.getSecretIds().stream())
                .anyMatch(id -> id.equals(secret.getId()));

        if (!hasAccess) {
            throw new RuntimeException("You do not have access to view this specific secret");
        }
    }

    // VAULT EDIT CHECK (For creating secrets - needs generic vault Editor role)
    private void checkVaultEditAccess(String userEmail, Long vaultId){
        Vault vault = vaultRepository.findById(vaultId)
                .orElseThrow(() -> new RuntimeException("Vault not found"));

        if(vault.getOwner().getEmail().equals(userEmail)){
            return;
        }

        VaultAccess access = vaultAccessRepository
                .findByUserEmailAndVaultId(userEmail, vaultId)
                .orElseThrow(() -> new RuntimeException("No access"));

        if(access.getRole().equals("VIEWER")){
            throw new RuntimeException("You only have VIEW access");
        }
    }

    //  OWNER ONLY DELETE
    private void checkOwnerAccess(String userEmail, Long vaultId){

        Vault vault = vaultRepository.findById(vaultId)
                .orElseThrow(() -> new RuntimeException("Vault not found"));

        if(!vault.getOwner().getEmail().equals(userEmail)){
            throw new RuntimeException("Only owner can delete");
        }
    }

    //  CREATE SECRET
    @Override
    public void createSecret(CreateSecretRequest request, String userEmail) {

        checkVaultEditAccess(userEmail, request.getVaultId());

        Vault vault = vaultRepository.findById(request.getVaultId())
                .orElseThrow(() -> new RuntimeException("Vault not found"));

        String encrypted = encryptionService.encrypt(
                request.getValue(),
                vault.getMasterKey()
        );

        LocalDateTime expiry = null;
        if (request.getExpiryDays() != null && request.getExpiryDays() != -1){
            expiry = LocalDateTime.now().plusDays(request.getExpiryDays());
        }

        Secret secret = Secret.builder()
                .name(request.getName())
                .encryptedValue(encrypted)
                .createdAt(LocalDateTime.now())
                .expiryTime(expiry)
                .type(request.getType())
                .vault(vault)
                .build();

        secretRepository.save(secret);
        auditLogService.log(userEmail, "SECRET_CREATED", secret.getName(), "New secret added to vault", "SUCCESS", vault.getOwner().getEmail());
    }

    //  GET SECRETS
    @Override
    public List<SecretResonse> getSecretByVault(Long vaultId) {

        List<Secret> secrets = secretRepository.findByVaultId(vaultId);

        return secrets.stream()
                .map(secret -> SecretResonse.builder()
                        .id(secret.getId())
                        .name(secret.getName())
                        .type(secret.getType())
                        .createdAt(secret.getCreatedAt())
                        .expiryTime(secret.getExpiryTime())
                        .build()
                ).toList();
    }

    //  DECRYPT SECRET
    @Override
    public String getDecryptedSecret(Long id, String password, Authentication auth) {

        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("user not found"));

        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new RuntimeException("Invalid Password");
        }

        Secret secret = secretRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Secret Not found"));

        // Validate if user has permission to view this specific secret
        checkSecretViewAccess(email, secret);

        try {
            String decrypted = encryptionService.decrypt(
                    secret.getEncryptedValue(),
                    secret.getVault().getMasterKey()
            );
            auditLogService.log(email, "SECRET_VIEWED", secret.getName(), "Secret decrypted and viewed", "SUCCESS", secret.getVault().getOwner().getEmail());
            return decrypted;
        } catch (Exception e) {
            auditLogService.log(email, "SECRET_VIEWED", secret.getName(), "Decryption failed", "FAILED", secret.getVault().getOwner().getEmail());
            throw new RuntimeException("Decryption failed", e);
        }
    }

    // DELETE (ONLY OWNER)
    @Override
    public void deleteSecret(Long id, String userEmail) {

        Secret secret = secretRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Secret Not found"));

        checkOwnerAccess(userEmail, secret.getVault().getId());

        secretRepository.delete(secret);

        auditLogService.log(userEmail, "SECRET_DELETED", secret.getName(), "Secret removed from vault", "SUCCESS", secret.getVault().getOwner().getEmail());
    }

    // UPDATE (EDITOR + OWNER)
    @Override
    public void updateSecret(Long id, UpdateSecretRequest request, String userEmail) {

        Secret secret = secretRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("secret not found"));

        checkSecretEditAccess(userEmail, secret);

        if (request.getName() != null){
            secret.setName(request.getName());
        }

        if(request.getType() != null){
            secret.setType(request.getType());
        }

        if(request.getValue() != null && !request.getValue().isEmpty()){
            String encrypted = encryptionService.encrypt(
                    request.getValue(),
                    secret.getVault().getMasterKey()
            );
            secret.setEncryptedValue(encrypted);
        }

        if(request.getExpiryDays() != null){
            if(request.getExpiryDays() == -1){
                secret.setExpiryTime(null);
            } else {
                secret.setExpiryTime(
                        LocalDateTime.now().plusDays(request.getExpiryDays())
                );
            }
        }

        secretRepository.save(secret);
        auditLogService.log(userEmail, "SECRET_UPDATED", secret.getName(), "Secret details updated", "SUCCESS", secret.getVault().getOwner().getEmail());
    }
}