package com.indore.repository;

import com.indore.entity.Secret;
import com.indore.entity.Vault;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SecretRepository extends JpaRepository<Secret,Long> {
    List<Secret> findByVaultId(Long vault);
    List<Secret> findByIdIn(List<Long> ids);
}
