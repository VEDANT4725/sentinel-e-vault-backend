package com.indore.repository;

import com.indore.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUserEmailOrOwnerEmailOrderByTimestampDesc(String userEmail, String ownerEmail);
}

