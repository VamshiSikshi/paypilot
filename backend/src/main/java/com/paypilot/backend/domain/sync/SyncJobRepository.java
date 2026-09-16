package com.paypilot.backend.domain.sync;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SyncJobRepository extends JpaRepository<SyncJob, Long> {

    List<SyncJob> findByMerchantIdOrderByCreatedAtDesc(Long merchantId);
}