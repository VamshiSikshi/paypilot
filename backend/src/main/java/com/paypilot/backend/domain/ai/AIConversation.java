package com.paypilot.backend.domain.ai;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.user.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "ai_conversations")
public class AIConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AIConversationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "last_message_at")
    private Instant lastMessageAt;

    protected AIConversation() {
    }

    public AIConversation(
            Merchant merchant,
            User user,
            String title
    ) {
        this.merchant = merchant;
        this.user = user;
        this.title = title;
        this.status = AIConversationStatus.ACTIVE;

        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public User getUser() {
        return user;
    }

    public String getTitle() {
        return title;
    }

    public AIConversationStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getLastMessageAt() {
        return lastMessageAt;
    }

    public void archive() {
        this.status = AIConversationStatus.ARCHIVED;
        this.updatedAt = Instant.now();
    }
}