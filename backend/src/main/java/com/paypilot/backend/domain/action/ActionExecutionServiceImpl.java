package com.paypilot.backend.domain.action;

import com.paypilot.backend.domain.action.dto.ActionExecutionResponse;
import com.paypilot.backend.domain.ai.AIConversation;
import com.paypilot.backend.domain.ai.AIConversationRepository;
import com.paypilot.backend.domain.ai.AIMessage;
import com.paypilot.backend.domain.ai.AIMessageRepository;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import com.paypilot.backend.domain.user.User;
import com.paypilot.backend.domain.user.UserRepository;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ActionExecutionServiceImpl
        implements ActionExecutionService {

    private final ActionExecutionRepository actionExecutionRepository;
    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final AIConversationRepository conversationRepository;
    private final AIMessageRepository messageRepository;

    public ActionExecutionServiceImpl(
            ActionExecutionRepository actionExecutionRepository,
            MerchantRepository merchantRepository,
            UserRepository userRepository,
            AIConversationRepository conversationRepository,
            AIMessageRepository messageRepository
    ) {
        this.actionExecutionRepository = actionExecutionRepository;
        this.merchantRepository = merchantRepository;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    public ActionExecutionResponse createExecution(
            Long merchantId,
            Long userId,
            Long conversationId,
            Long messageId,
            ActionType actionType,
            ActionTargetType targetType,
            Long targetId,
            Boolean confirmationRequired,
            String requestData,
            String idempotencyKey
    ) {
        var merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Merchant not found"));

        User user = userRepository
                .findByMerchantIdAndId(merchantId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (messageId != null && conversationId == null) {
            throw new IllegalArgumentException(
                    "Conversation is required when message is provided"
            );
        }

        AIConversation conversation = null;
        if (conversationId != null) {
            conversation = conversationRepository
                    .findByMerchantIdAndId(merchantId, conversationId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "AI conversation not found"
                            ));
        }

        AIMessage message = null;
        if (messageId != null) {
            message = messageRepository
                    .findByMerchantIdAndConversationIdAndId(
                            merchantId,
                            conversationId,
                            messageId
                    )
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "AI message not found"
                            ));
        }

        if (idempotencyKey != null) {
            var existingExecution =
                    actionExecutionRepository
                            .findByMerchantIdAndIdempotencyKey(
                                    merchantId,
                                    idempotencyKey
                            );

            if (existingExecution.isPresent()) {
                return ActionExecutionResponse.from(
                        existingExecution.get()
                );
            }
        }

        ActionExecution execution = new ActionExecution(
                merchant,
                user,
                conversation,
                message,
                actionType,
                targetType,
                targetId,
                confirmationRequired,
                requestData,
                idempotencyKey
        );

        return ActionExecutionResponse.from(
                actionExecutionRepository.save(execution)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ActionExecutionResponse getExecution(
            Long merchantId,
            Long executionId
    ) {
        ActionExecution execution = actionExecutionRepository
                .findByMerchantIdAndId(merchantId, executionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Action execution not found"
                        ));

        return ActionExecutionResponse.from(execution);
    }

    @Override
    @Transactional(readOnly = true)
    public ActionExecutionResponse getExecutionByIdempotencyKey(
            Long merchantId,
            String idempotencyKey
    ) {
        ActionExecution execution = actionExecutionRepository
                .findByMerchantIdAndIdempotencyKey(
                        merchantId,
                        idempotencyKey
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Action execution not found"
                        ));

        return ActionExecutionResponse.from(execution);
    }

    @Override
    public ActionExecutionResponse confirmExecution(
            Long merchantId,
            Long executionId
    ) {
        ActionExecution execution = actionExecutionRepository
                .findByMerchantIdAndId(merchantId, executionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Action execution not found"
                        ));

        execution.confirm();

        return ActionExecutionResponse.from(execution);
    }

    @Override
    public ActionExecutionResponse startExecution(
            Long merchantId,
            Long executionId
    ) {
        ActionExecution execution = actionExecutionRepository
                .findByMerchantIdAndId(merchantId, executionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Action execution not found"
                        ));

        execution.start();

        return ActionExecutionResponse.from(execution);
    }

    @Override
    public ActionExecutionResponse completeExecution(
            Long merchantId,
            Long executionId,
            String resultData
    ) {
        ActionExecution execution = actionExecutionRepository
                .findByMerchantIdAndId(merchantId, executionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Action execution not found"
                        ));

        execution.complete(resultData);

        return ActionExecutionResponse.from(execution);
    }

    @Override
    public ActionExecutionResponse failExecution(
            Long merchantId,
            Long executionId,
            String errorMessage
    ) {
        ActionExecution execution = actionExecutionRepository
                .findByMerchantIdAndId(merchantId, executionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Action execution not found"
                        ));

        execution.fail(errorMessage);

        return ActionExecutionResponse.from(execution);
    }

    @Override
    public ActionExecutionResponse cancelExecution(
            Long merchantId,
            Long executionId
    ) {
        ActionExecution execution = actionExecutionRepository
                .findByMerchantIdAndId(merchantId, executionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Action execution not found"
                        ));

        execution.cancel();

        return ActionExecutionResponse.from(execution);
    }
}