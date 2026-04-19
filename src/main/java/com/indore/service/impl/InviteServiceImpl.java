package com.indore.service.impl;

import com.indore.dto.CreateInviteRequest;
import com.indore.dto.InviteResponse;
import com.indore.entity.Invite;
import com.indore.entity.Vault;
import com.indore.entity.VaultAccess;
import com.indore.repository.InviteRepository;
import com.indore.repository.SecretRepository;
import com.indore.repository.VaultAccessRepository;
import com.indore.repository.VaultRepository;
import com.indore.service.AuditLogService;
import com.indore.service.EmailService;
import com.indore.service.InviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InviteServiceImpl implements InviteService {
    private final InviteRepository inviteRepository;
    private final EmailService emailService;
    private final VaultRepository vaultRepository;
    private final VaultAccessRepository vaultAccessRepository;
    private final SecretRepository secretRepository;
    private final AuditLogService auditLogService;

    @Override
    public void createInvite(CreateInviteRequest request, String userEmail) {

        String email = request.getEmail().trim().toLowerCase();

        Vault vault = vaultRepository.findById(request.getVaultId())
                .orElseThrow(() -> new RuntimeException("vault not found"));

        List<Long> secretIds = request.getSecretIds();

        // If scope is FULL, auto-fetch all secrets from the vault
        if ("FULL".equalsIgnoreCase(request.getScope())) {
            secretIds = secretRepository.findByVaultId(request.getVaultId())
                    .stream()
                    .map(secret -> secret.getId())
                    .toList();
        }

        if (secretIds == null || secretIds.isEmpty()) {
            throw new RuntimeException("No secrets found in this vault to share");
        }

        // Collect all secret IDs that already have a PENDING invite for this email+vault
        List<Invite> existingInvites = inviteRepository
                .findByEmailAndVaultIdAndStatus(email, request.getVaultId(), "PENDING");
        Set<Long> alreadySharedSecretIds = new HashSet<>();
        for (Invite inv : existingInvites) {
            if (inv.getSecretIds() != null) {
                alreadySharedSecretIds.addAll(inv.getSecretIds());
            }
        }

        // Create one invite per secret so each can be accepted/rejected individually
        for (Long secretId : secretIds) {
            if (alreadySharedSecretIds.contains(secretId)) {
                continue; // Skip duplicate
            }

            Invite invite = Invite.builder()
                    .email(email)
                    .role(request.getRole())
                    .scope(request.getScope())
                    .vaultId(request.getVaultId())
                    .vaultName(vault.getName())
                    .invitedBy(userEmail)
                    .secretIds(List.of(secretId))
                    .status("PENDING")
                    .createdAt(LocalDateTime.now())
                    .build();

            inviteRepository.save(invite);
        }

        auditLogService.log(userEmail, "INVITE_SENT", vault.getName(), "Invitation sent to: " + email, "SUCCESS", vault.getOwner().getEmail());
    }

    @Override
    public List<InviteResponse> getPendingInvites(String email) {
        List<Invite> invites = inviteRepository.findByEmailAndStatus(email, "PENDING");

        return invites.stream().map(invite -> {
            List<String> secretNames = List.of();
            if (invite.getSecretIds() != null && !invite.getSecretIds().isEmpty()) {
                secretNames = secretRepository.findByIdIn(invite.getSecretIds())
                        .stream()
                        .map(secret -> secret.getName())
                        .toList();
            }
            return InviteResponse.builder()
                    .id(invite.getId())
                    .vaultName(invite.getVaultName())
                    .role(invite.getRole())
                    .invitedBy(invite.getInvitedBy())
                    .status(invite.getStatus())
                    .email(invite.getEmail())
                    .secretNames(secretNames)
                    .build();
        }).toList();
    }

    @Override
    public List<InviteResponse> getSentInvites(String email) {
        List<Invite> invites = inviteRepository.findByInvitedByOrderByCreatedAtDesc(email);

        return invites.stream().map(invite -> {
            List<String> secretNames = List.of();
            if (invite.getSecretIds() != null && !invite.getSecretIds().isEmpty()) {
                secretNames = secretRepository.findByIdIn(invite.getSecretIds())
                        .stream()
                        .map(secret -> secret.getName())
                        .toList();
            }
            return InviteResponse.builder()
                    .id(invite.getId())
                    .vaultName(invite.getVaultName())
                    .role(invite.getRole())
                    .invitedBy(invite.getInvitedBy())
                    .status(invite.getStatus())
                    .email(invite.getEmail())
                    .secretNames(secretNames)
                    .build();
        }).toList();
    }

    @Override
    public void acceptInvite(Long inviteId, String userEmail) {
        Invite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invite not found"));

        String inviteEmail = invite.getEmail().trim().toLowerCase();
        String cuurentUser = userEmail.trim().toLowerCase();
        if (!inviteEmail.equals(cuurentUser)) {
            throw new RuntimeException("you are not allowed to accept this invite");
        }

        invite.setStatus("ACCEPTED");
        inviteRepository.save(invite);

        Optional<VaultAccess> existingAccess = vaultAccessRepository.findByUserEmailAndVaultId(userEmail, invite.getVaultId());

        if (existingAccess.isPresent()) {
            VaultAccess access = existingAccess.get();
            access.setRole(invite.getRole());
            access.setSharedBy(invite.getInvitedBy());
            vaultAccessRepository.save(access);
        } else {
            VaultAccess access = VaultAccess.builder()
                    .userEmail(userEmail)
                    .vaultId(invite.getVaultId())
                    .role(invite.getRole())
                    .sharedBy(invite.getInvitedBy())
                    .build();

            vaultAccessRepository.save(access);
        }

        Vault vault = vaultRepository.findById(invite.getVaultId()).orElse(null);
        String ownerEmail = (vault != null) ? vault.getOwner().getEmail() : null;
        auditLogService.log(userEmail, "INVITE_ACCEPTED", invite.getVaultName(), "Secret access invitation accepted", "SUCCESS", ownerEmail);
    }

    @Override
    public void rejectInvite(Long inviteId, String userEmail) {
        Invite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invite not found"));

        String inviteEmail = invite.getEmail().trim().toLowerCase();
        String cuurentUser = userEmail.trim().toLowerCase();
        if (!inviteEmail.equals(cuurentUser)) {
            throw new RuntimeException("you are not allowed to reject this invite");
        }

        invite.setStatus("REJECTED");
        inviteRepository.save(invite);

        Vault vault = vaultRepository.findById(invite.getVaultId()).orElse(null);
        String ownerEmail = (vault != null) ? vault.getOwner().getEmail() : null;
        auditLogService.log(userEmail, "INVITE_REJECTED", invite.getVaultName(), "Secret access invitation declined", "SUCCESS", ownerEmail);
    }
}
