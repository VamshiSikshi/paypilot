
package com.paypilot.backend.controller;

import com.paypilot.backend.domain.sync.SyncService;
import com.paypilot.backend.domain.sync.dto.SyncJobResponse;
import com.paypilot.backend.security.AuthenticatedUser;
import com.paypilot.backend.security.AuthenticatedUserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sync/jobs")
public class SyncController {

    private final SyncService syncService;
    private final AuthenticatedUserContext authenticatedUserContext;

    public SyncController(
            SyncService syncService,
            AuthenticatedUserContext authenticatedUserContext
    ) {
        this.syncService = syncService;
        this.authenticatedUserContext = authenticatedUserContext;
    }

    @GetMapping
    public List<SyncJobResponse> getSyncJobs() {
        AuthenticatedUser user =
                authenticatedUserContext.getCurrentUser();

        return syncService.getSyncJobs(user.merchantId());
    }
}
