package com.indore.repository;

import com.indore.entity.Invite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InviteRepository extends JpaRepository<Invite, Long> {
    List<Invite> findByEmailAndStatus(String email, String status);
    List<Invite> findByEmailAndVaultIdAndStatus(String email, Long vaultId, String status);
    boolean existsByEmailAndVaultIdAndStatus(String email, Long vaultId, String status);

    List<Invite> findByInvitedByOrderByCreatedAtDesc(String invitedBy);
    Optional<Invite> findByEmailAndVaultId(String email, Long vaultId);
}
