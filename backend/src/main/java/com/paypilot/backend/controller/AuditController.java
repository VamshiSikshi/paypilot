
package com.paypilot.backend.controller;

import com.paypilot.backend.domain.audit.AuditLogService;
import com.paypilot.backend.domain.audit.dto.AuditLogResponse;
import com.paypilot.backend.security.AuthenticatedUser;
import com.paypilot.backend.security.AuthenticatedUserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditController {

    private final AuditLogService auditLogService;
    private final AuthenticatedUserContext authenticatedUserContext;

    public AuditController(
            AuditLogService auditLogService,
            AuthenticatedUserContext authenticatedUserContext
    ) {
        this.auditLogService = auditLogService;
        this.authenticatedUserContext = authenticatedUserContext;
    }

    @GetMapping
    public List<AuditLogResponse> getAuditLogs() {
        AuthenticatedUser user =
                authenticatedUserContext.getCurrentUser();

        return auditLogService.getAuditLogs(user.merchantId());
    }
}
