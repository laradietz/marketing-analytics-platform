package com.marketinganalytics.platform.entity;

import com.marketinganalytics.platform.entity.enums.ConnectionStatus;
import com.marketinganalytics.platform.entity.enums.IntegrationProvider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Credentials and sync state for a real ad-platform account connected to one
 * of our {@link Platform} rows. Tokens are stored encrypted at rest (see
 * {@code CredentialCipher}) — never logged or returned by the API in plain
 * text.
 */
@Entity
@Table(name = "platform_connections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "platform_id", nullable = false, unique = true)
    private Platform platform;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IntegrationProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ConnectionStatus status = ConnectionStatus.DISCONNECTED;

    @Column(name = "external_account_id", length = 120)
    private String externalAccountId;

    @Column(name = "access_token_encrypted", columnDefinition = "TEXT")
    private String accessTokenEncrypted;

    @Column(name = "refresh_token_encrypted", columnDefinition = "TEXT")
    private String refreshTokenEncrypted;

    @Column(name = "token_expires_at")
    private Instant tokenExpiresAt;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @Column(name = "last_sync_message", length = 500)
    private String lastSyncMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
