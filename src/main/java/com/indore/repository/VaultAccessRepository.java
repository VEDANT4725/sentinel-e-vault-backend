package com.indore.repository;

import com.indore.entity.VaultAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VaultAccessRepository extends JpaRepository<VaultAccess, Long> {
    List<VaultAccess> findByUserEmail(String userEmail);
    Optional<VaultAccess> findByUserEmailAndVaultId(String userEmail, Long vaultId);
}
