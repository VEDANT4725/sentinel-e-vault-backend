package com.indore.service;

import com.indore.entity.AuditLog;

import java.util.List;

public interface AuditLogService {
    void log(String userEmail, String action, String resource, String details, String status, String ownerEmail);
    List<AuditLog> getLogsForUser(String email);
}
