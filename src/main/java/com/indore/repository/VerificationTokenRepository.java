package com.indore.repository;

import com.indore.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken,Long> {
    // token se verify karne ke liye
    Optional<VerificationToken> findByToken(String token);
}
