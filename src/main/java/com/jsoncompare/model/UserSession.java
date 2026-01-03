package com.jsoncompare.model;

import com.jsoncompare.model.enums.UserSessionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_sessions", indexes = {
        @Index(name = "idx_auth_token", columnList = "auth_token"),
        @Index(name = "idx_expires_at", columnList = "expires_at"),
        @Index(name = "idx_user_status", columnList = "user_id, status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "uuid")
    private User user;

    @Column(name = "auth_token", nullable = false, columnDefinition = "TEXT")
    private String authToken;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean deleted = false;

    @Column(name = "admin_login", nullable = false)
    private Boolean adminLogin = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserSessionStatus status = UserSessionStatus.ACTIVE;

    // Helper methods

    /**
     * Check if the session is still valid
     * Valid = not deleted AND status is ACTIVE AND not expired
     */
    public boolean isValid() {
        return !deleted
                && status == UserSessionStatus.ACTIVE
                && LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Invalidate the session (soft invalidation)
     */
    public void invalidate() {
        this.status = UserSessionStatus.EXPIRED;
    }

    /**
     * Revoke the session (usually for security reasons)
     */
    public void revoke() {
        this.status = UserSessionStatus.REVOKED;
    }

    /**
     * Mark session as logged out
     */
    public void logout() {
        this.status = UserSessionStatus.LOGGED_OUT;
    }

    /**
     * Check if session has expired by time
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if this is an admin session
     */
    public boolean isAdminSession() {
        return adminLogin;
    }
}