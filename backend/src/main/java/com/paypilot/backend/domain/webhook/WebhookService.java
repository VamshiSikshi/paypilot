package com.paypilot.backend.domain.webhook;

import com.paypilot.backend.domain.webhook.dto.WebhookResponse;

public interface WebhookService {

    WebhookResponse getWebhook(Long merchantId, Long webhookId);
}