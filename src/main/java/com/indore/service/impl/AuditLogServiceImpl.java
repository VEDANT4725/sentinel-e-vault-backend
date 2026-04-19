package com.indore.service.impl;

import com.indore.entity.AuditLog;
import com.indore.repository.AuditLogRepository;
import com.indore.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void log(String userEmail, String action, String resource, String details, String status, String ownerEmail) {
        AuditLog auditLog = AuditLog.builder()
                .userEmail(userEmail)
                .ownerEmail(ownerEmail)
                .action(action)
                .resource(resource)
                .details(details)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(auditLog);
    }

    @Override
    public List<AuditLog> getLogsForUser(String email) {
        return auditLogRepository.findByUserEmailOrOwnerEmailOrderByTimestampDesc(email, email);
    }
}
