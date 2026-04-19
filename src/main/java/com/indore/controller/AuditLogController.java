package com.indore.controller;

import com.indore.entity.AuditLog;
import com.indore.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public List<AuditLog> getAuditLogs(Authentication authentication) {
        String email = authentication.getName().toLowerCase();
        return auditLogService.getLogsForUser(email);
    }
}
