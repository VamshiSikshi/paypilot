package com.paypilot.backend.domain.sync;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.flyway.enabled=false")
class SyncJobRepositoryTest {

    @Autowired
    private SyncJobRepository syncJobRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Test
    void shouldSaveAndFindSyncJobsByMerchant() {
        Merchant merchant = merchantRepository.save(
                new Merchant(
                        "acc_sync_001",
                        "Sync Business",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        SyncJob completedJob = syncJobRepository.save(
                new SyncJob(
                        merchant,
                        SyncResourceType.PAYMENTS,
                        SyncTriggerType.MANUAL
                )
        );

        SyncJob pendingJob = syncJobRepository.save(
                new SyncJob(
                        merchant,
                        SyncResourceType.ORDERS,
                        SyncTriggerType.SCHEDULED
                )
        );

        List<SyncJob> jobs =
                syncJobRepository.findByMerchantIdOrderByCreatedAtDesc(
                        merchant.getId()
                );

        assertThat(jobs).hasSize(2);
        assertThat(jobs).contains(completedJob, pendingJob);

        assertThat(completedJob.getMerchant().getId())
                .isEqualTo(merchant.getId());

        assertThat(completedJob.getResourceType())
                .isEqualTo(SyncResourceType.PAYMENTS);

        assertThat(completedJob.getStatus())
                .isEqualTo(SyncJobStatus.PENDING);

        assertThat(completedJob.getTriggerType())
                .isEqualTo(SyncTriggerType.MANUAL);

        assertThat(completedJob.getRecordsProcessed())
                .isZero();

        assertThat(completedJob.getRecordsCreated())
                .isZero();

        assertThat(completedJob.getRecordsUpdated())
                .isZero();

        assertThat(completedJob.getStartedAt())
                .isNull();

        assertThat(completedJob.getCompletedAt())
                .isNull();

        assertThat(completedJob.getErrorMessage())
                .isNull();

        assertThat(completedJob.getCreatedAt())
                .isNotNull();

        assertThat(completedJob.getUpdatedAt())
                .isNotNull();
    }
}
