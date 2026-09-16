package com.paypilot.backend.domain.ai;

import com.paypilot.backend.domain.merchant.Merchant;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "ai_messages")
public class AIMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private AIConversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AIMessageRole role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 30)
    private AIMessageType messageType;

    @Column
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AIMessage() {
    }

    public AIMessage(
            AIConversation conversation,
            Merchant merchant,
            AIMessageRole role,
            String content,
            AIMessageType messageType
    ) {
        this.conversation = conversation;
        this.merchant = merchant;
        this.role = role;
        this.content = content;
        this.messageType = messageType;

        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public AIConversation getConversation() {
        return conversation;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public AIMessageRole getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public AIMessageType getMessageType() {
        return messageType;
    }

    public String getMetadata() {
        return metadata;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}