package com.paypilot.backend.domain.webhook;

import com.paypilot.backend.domain.webhook.dto.WebhookResponse;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WebhookServiceImpl implements WebhookService {

    private final WebhookRepository webhookRepository;

    public WebhookServiceImpl(WebhookRepository webhookRepository) {
        this.webhookRepository = webhookRepository;
    }

    @Override
    public WebhookResponse getWebhook(Long merchantId, Long webhookId) {
        Webhook webhook = webhookRepository
                .findByMerchantIdAndId(merchantId, webhookId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Webhook not found: " + webhookId
                        )
                );

        return WebhookResponse.from(webhook);
    }
}