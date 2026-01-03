package com.jsoncompare.repository;

import com.jsoncompare.model.ComparisonDiff;
import com.jsoncompare.model.enums.ComparisonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ComparisonDiffRepository extends JpaRepository<ComparisonDiff, UUID> {

    // Find by ID and not deleted
    Optional<ComparisonDiff> findByIdAndDeletedFalse(UUID id);

    // Find by ID, user, and not deleted
    Optional<ComparisonDiff> findByIdAndCreatedByIdAndDeletedFalse(UUID id, UUID userId);

    // Find all by user (paginated)
    Page<ComparisonDiff> findByCreatedByIdAndDeletedFalseOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    // Find all by user and status
    Page<ComparisonDiff> findByCreatedByIdAndComparisonStatusAndDeletedFalseOrderByCreatedAtDesc(
            UUID userId, ComparisonStatus status, Pageable pageable);

    // Find all by user (list)
    List<ComparisonDiff> findByCreatedByIdAndDeletedFalseOrderByCreatedAtDesc(UUID userId);

    // Count by user
    long countByCreatedByIdAndDeletedFalse(UUID userId);
}

