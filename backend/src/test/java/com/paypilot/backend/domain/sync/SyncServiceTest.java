package com.paypilot.backend.domain.sync;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.sync.dto.SyncJobResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncServiceTest {

    @Mock
    private SyncJobRepository syncJobRepository;

    @InjectMocks
    private SyncServiceImpl syncService;

    @Test
    void shouldGetSyncJobsForMerchant() {
        Long merchantId = 1L;

        Merchant merchant = mock(Merchant.class);
        when(merchant.getId()).thenReturn(merchantId);

        SyncJob paymentJob = new SyncJob(
                merchant,
                SyncResourceType.PAYMENTS,
                SyncTriggerType.MANUAL
        );

        SyncJob orderJob = new SyncJob(
                merchant,
                SyncResourceType.ORDERS,
                SyncTriggerType.SCHEDULED
        );

        when(syncJobRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId))
                .thenReturn(List.of(paymentJob, orderJob));

        List<SyncJobResponse> responses = syncService.getSyncJobs(merchantId);

        assertThat(responses).hasSize(2);

        assertThat(responses.get(0).merchantId()).isEqualTo(merchantId);
        assertThat(responses.get(0).resourceType())
                .isEqualTo(SyncResourceType.PAYMENTS);
        assertThat(responses.get(0).status())
                .isEqualTo(SyncJobStatus.PENDING);
        assertThat(responses.get(0).triggerType())
                .isEqualTo(SyncTriggerType.MANUAL);

        assertThat(responses.get(1).merchantId()).isEqualTo(merchantId);
        assertThat(responses.get(1).resourceType())
                .isEqualTo(SyncResourceType.ORDERS);
        assertThat(responses.get(1).triggerType())
                .isEqualTo(SyncTriggerType.SCHEDULED);

        verify(syncJobRepository)
                .findByMerchantIdOrderByCreatedAtDesc(merchantId);
    }

    @Test
    void shouldReturnEmptyListWhenMerchantHasNoSyncJobs() {
        Long merchantId = 1L;

        when(syncJobRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId))
                .thenReturn(List.of());

        List<SyncJobResponse> responses = syncService.getSyncJobs(merchantId);

        assertThat(responses).isEmpty();

        verify(syncJobRepository)
                .findByMerchantIdOrderByCreatedAtDesc(merchantId);
    }

    @Test
    void shouldOnlyQueryJobsForRequestedMerchant() {
        Long merchantId = 2L;

        when(syncJobRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId))
                .thenReturn(List.of());

        List<SyncJobResponse> responses = syncService.getSyncJobs(merchantId);

        assertThat(responses).isEmpty();

        verify(syncJobRepository)
                .findByMerchantIdOrderByCreatedAtDesc(merchantId);
        verifyNoMoreInteractions(syncJobRepository);
    }
}