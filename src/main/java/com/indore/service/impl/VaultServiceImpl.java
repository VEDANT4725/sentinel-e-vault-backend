package com.indore.service.impl;

import com.indore.dto.CreateVaultRequest;
import com.indore.dto.SecretDto;
import com.indore.dto.SharedVaultResponse;
import com.indore.dto.VaultResponse;
import com.indore.entity.*;
import com.indore.repository.*;
import com.indore.service.AuditLogService;
import com.indore.service.VaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class VaultServiceImpl implements VaultService {

    private final VaultRepository vaultRepository;
    private final UserRepository userRepository;
    private final VaultAccessRepository vaultAccessRepository;
    private final InviteRepository inviteRepository;
    private final SecretRepository secretRepository;
    private final AuditLogService auditLogService;


    @Override
    public void createVault(CreateVaultRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            //Generate master key
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);

            SecretKey key = keyGen.generateKey();

            String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());

            //Set inside vault
            Vault vault = Vault.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .encryptionType(request.getEncryptionType())
                    .status("ACTIVE")
                    .vaultCode("VLT_" + UUID.randomUUID().toString().substring(0, 6))
                    .createdAt(LocalDateTime.now())
                    .masterKey(base64Key)
                    .owner(user)
                    .build();

            vaultRepository.save(vault);
            auditLogService.log(email, "VAULT_CREATED", vault.getName(), "Vault successfully initialized", "SUCCESS", email);

        } catch (Exception e) {
            throw new RuntimeException("Vault creation failed", e);
        }
    }
    public List<VaultResponse> getVaults(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow();
        return vaultRepository.findByOwner(user)
                .stream()
                .map(vault -> VaultResponse.builder()
                        .id(vault.getId())
                        .name(vault.getName())
                        .description(vault.getDescription())
                        .status(vault.getStatus())
                        .vaultCode(vault.getVaultCode())
                        .lastAccessTime(vault.getLastAccessTime())
                        .secretsCount(0)
                        .membersCount(1)
                        .build()
                ).toList();

    }

    @Override
    public List<SharedVaultResponse> getSharedVaults(String userEmail) {

        List<VaultAccess> accesses = vaultAccessRepository.findByUserEmail(userEmail);

        return accesses.stream().map(access -> {


            Vault vault = vaultRepository.findById(access.getVaultId())
                    .orElseThrow(() -> new RuntimeException("Vault not found"));


            List<Invite> acceptedInvites = inviteRepository
                    .findByEmailAndVaultIdAndStatus(userEmail, access.getVaultId(), "ACCEPTED");

            if (acceptedInvites.isEmpty()) return null; // Skip if no accepted invites

            // Collect all secret IDs from all accepted invites
            List<Long> allSecretIds = acceptedInvites.stream()
                    .filter(inv -> inv.getSecretIds() != null)
                    .flatMap(inv -> inv.getSecretIds().stream())
                    .distinct()
                    .toList();

            List<Secret> secrets = allSecretIds.isEmpty() ? List.of() : secretRepository.findByIdIn(allSecretIds);

            String role = acceptedInvites.get(0).getRole();

            List<SecretDto> secretDto = secrets.stream().map(secret ->
                    SecretDto.builder()
                            .id(secret.getId())
                            .key(secret.getName())
                            .value(secret.getEncryptedValue())
                            .type(secret.getType())
                            .build()
            ).toList();

            return SharedVaultResponse.builder()
                    .vaultName(vault.getName())
                    .sharedBy(access.getSharedBy())
                    .role(access.getRole())
                    .secrets(secretDto)
                    .build();

        }).filter(Objects::nonNull).toList();
    }
}
