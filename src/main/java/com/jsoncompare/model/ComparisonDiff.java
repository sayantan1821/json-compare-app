package com.jsoncompare.model;

import com.jsoncompare.model.enums.ComparisonStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "comparison_diffs", indexes = {
        @Index(name = "idx_created_by_created", columnList = "created_by_id, created_at"),
        @Index(name = "idx_comparison_status", columnList = "comparison_status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonDiff {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false, columnDefinition = "uuid")
    private User createdBy;

    @Column(name = "input_string_a", nullable = false, columnDefinition = "TEXT")
    private String inputStringA;

    @Column(name = "input_string_b", nullable = false, columnDefinition = "TEXT")
    private String inputStringB;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String result;

    @Column(nullable = false)
    private Boolean identical;

    @Enumerated(EnumType.STRING)
    @Column(name = "comparison_status", nullable = false, length = 50)
    private ComparisonStatus comparisonStatus = ComparisonStatus.COMPLETED;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_compared_at")
    private LocalDateTime lastComparedAt;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean deleted = false;

    // Helper methods

    /**
     * Soft delete the comparison
     * Sets deleted flag and archives the status
     */
    public void softDelete() {
        this.deleted = true;
    }

    /**
     * Restore a soft-deleted comparison
     */
    public void restore() {
        this.deleted = false;
    }

    /**
     * Check if comparison is deleted
     */
    public boolean isDeleted() {
        return this.deleted;
    }

    /**
     * Mark comparison as completed and update timestamp
     */
    public void markAsCompared() {
        this.lastComparedAt = LocalDateTime.now();
        this.comparisonStatus = ComparisonStatus.COMPLETED;
    }

    /**
     * Mark comparison as in progress
     */
    public void markAsInProgress() {
        this.comparisonStatus = ComparisonStatus.IN_PROGRESS;
    }

    /**
     * Mark comparison as failed
     */
    public void markAsFailed() {
        this.comparisonStatus = ComparisonStatus.FAILED;
    }

    /**
     * Archive the comparison
     */
    public void archive() {
        this.comparisonStatus = ComparisonStatus.ARCHIVED;
    }

    /**
     * Check if comparison was successful
     */
    public boolean isCompleted() {
        return this.comparisonStatus == ComparisonStatus.COMPLETED;
    }
}