package com.indore.repository;

import com.indore.entity.User;
import com.indore.entity.Vault;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VaultRepository extends JpaRepository<Vault, Long> {
    List<Vault> findByOwner(User user);
}
