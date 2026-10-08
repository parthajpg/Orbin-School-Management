package com.orbin.school.auth.entity;

import com.orbin.school.common.entity.BaseEntity;
import com.orbin.school.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Persisted refresh token supporting:
 * - rotation (each use generates a new token, old is revoked)
 * - logout (mark revoked)
 * - family detection (stolen token detection)
 */
@Entity
@Table(name = "refresh_tokens",
       indexes = {
           @Index(name = "idx_rt_token",   columnList = "token"),
           @Index(name = "idx_rt_user_id", columnList = "user_id")
       })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RefreshToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    /** Token family: same value for all rotations of an original session. Used for theft detection. */
    @Column(name = "family", nullable = false, length = 100)
    private String family;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked", nullable = false)
    @Builder.Default
    private boolean revoked = false;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "user_agent", length = 300)
    private String userAgent;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }

    public void revoke() {
        this.revoked = true;
        this.revokedAt = Instant.now();
    }
}
