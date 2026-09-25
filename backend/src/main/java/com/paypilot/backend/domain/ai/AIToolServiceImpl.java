package com.paypilot.backend.domain.ai;

import com.paypilot.backend.domain.merchant.MerchantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.paypilot.backend.exception.ResourceNotFoundException;

@Service
@Transactional
public class AIToolServiceImpl implements AIToolService {

    private final AIToolExecutionRepository toolExecutionRepository;
    private final MerchantRepository merchantRepository;
    private final AIConversationRepository conversationRepository;
    private final AIMessageRepository messageRepository;

    public AIToolServiceImpl(
            AIToolExecutionRepository toolExecutionRepository,
            MerchantRepository merchantRepository,
            AIConversationRepository conversationRepository,
            AIMessageRepository messageRepository
    ) {
        this.toolExecutionRepository = toolExecutionRepository;
        this.merchantRepository = merchantRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    public AIToolExecution createExecution(
            Long merchantId,
            Long conversationId,
            Long messageId,
            String toolName,
            String toolCallId,
            String input
    ) {
        var merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Merchant not found"));

        var conversation = conversationRepository
                .findByMerchantIdAndId(merchantId, conversationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("AI conversation not found"));

        var message = messageRepository
                .findByMerchantIdAndConversationIdOrderByCreatedAtAsc(
                        merchantId,
                        conversationId
                )
                .stream()
                .filter(existingMessage -> existingMessage.getId().equals(messageId))
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException("AI message not found"));

        AIToolExecution execution = new AIToolExecution(
                merchant,
                conversation,
                message,
                toolName,
                toolCallId,
                input
        );

        return toolExecutionRepository.save(execution);
    }

    @Override
    @Transactional(readOnly = true)
    public AIToolExecution getExecution(
            Long merchantId,
            Long executionId
    ) {
        return toolExecutionRepository
                .findByMerchantIdAndId(merchantId, executionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("AI tool execution not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public AIToolExecution getExecutionByToolCallId(
            Long merchantId,
            String toolCallId
    ) {
        return toolExecutionRepository
                .findByMerchantIdAndToolCallId(merchantId, toolCallId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("AI tool execution not found"));
    }

    @Override
    public AIToolExecution startExecution(
            Long merchantId,
            Long executionId
    ) {
        AIToolExecution execution = getExecution(merchantId, executionId);
        execution.start();
        return execution;
    }

    @Override
    public AIToolExecution completeExecution(
            Long merchantId,
            Long executionId,
            String output
    ) {
        AIToolExecution execution = getExecution(merchantId, executionId);
        execution.complete(output);
        return execution;
    }

    @Override
    public AIToolExecution failExecution(
            Long merchantId,
            Long executionId,
            String errorMessage
    ) {
        AIToolExecution execution = getExecution(merchantId, executionId);
        execution.fail(errorMessage);
        return execution;
    }
}