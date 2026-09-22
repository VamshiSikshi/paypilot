package com.paypilot.backend.domain.sync;

import com.paypilot.backend.domain.sync.dto.SyncJobResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SyncServiceImpl implements SyncService {

    private final SyncJobRepository syncJobRepository;

    public SyncServiceImpl(SyncJobRepository syncJobRepository) {
        this.syncJobRepository = syncJobRepository;
    }

    @Override
    public List<SyncJobResponse> getSyncJobs(Long merchantId) {
        return syncJobRepository
                .findByMerchantIdOrderByCreatedAtDesc(merchantId)
                .stream()
                .map(SyncJobResponse::from)
                .toList();
    }
}