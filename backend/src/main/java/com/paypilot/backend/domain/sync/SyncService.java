package com.paypilot.backend.domain.sync;

import com.paypilot.backend.domain.sync.dto.SyncJobResponse;

import java.util.List;

public interface SyncService {

    List<SyncJobResponse> getSyncJobs(Long merchantId);
}