package com.jsoncompare.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
import com.flipkart.zjsonpatch.DiffFlags;
import com.jsoncompare.dto.compare.*;
import com.jsoncompare.model.ComparisonDiff;
import com.jsoncompare.model.User;
import com.jsoncompare.model.enums.ComparisonStatus;
import com.jsoncompare.repository.ComparisonDiffRepository;
import com.jsoncompare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompareService {

    private final ComparisonDiffRepository comparisonDiffRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    private static final int PREVIEW_LENGTH = 100;
    private static final EnumSet<DiffFlags> DIFF_FLAGS = EnumSet.of(
            DiffFlags.ADD_ORIGINAL_VALUE_ON_REPLACE,
            DiffFlags.OMIT_MOVE_OPERATION
    );

    @Transactional
    public CompareResponse compare(CompareRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Parse and validate JSON inputs
        JsonNode jsonNodeA = parseJson(request.getJsonA(), "JSON A");
        JsonNode jsonNodeB = parseJson(request.getJsonB(), "JSON B");

        // Compute diff using zjsonpatch
        JsonNode diffNode = JsonDiff.asJson(jsonNodeA, jsonNodeB, DIFF_FLAGS);

        // Parse differences into structured format
        List<DiffDetail> differences = parseDiffDetails(diffNode, jsonNodeA, jsonNodeB);
        boolean identical = differences.isEmpty();

        // Create and save comparison entity
        ComparisonDiff comparison = new ComparisonDiff();
        comparison.setCreatedBy(user);
        comparison.setInputStringA(request.getJsonA());
        comparison.setInputStringB(request.getJsonB());
        comparison.setResult(diffNode.toString());
        comparison.setIdentical(identical);
        comparison.setComparisonStatus(ComparisonStatus.COMPLETED);
        comparison.setDescription(request.getDescription());
        comparison.setLastComparedAt(LocalDateTime.now());
        comparison.setDeleted(false);

        comparison = comparisonDiffRepository.save(comparison);
        log.info("Comparison created: {} (identical: {})", comparison.getId(), identical);

        return buildCompareResponse(comparison, differences);
    }

    @Transactional(readOnly = true)
    public CompareResponse getComparison(UUID comparisonId, UUID userId) {
        ComparisonDiff comparison = comparisonDiffRepository
                .findByIdAndCreatedByIdAndDeletedFalse(comparisonId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Comparison not found"));

        List<DiffDetail> differences = parseDiffDetailsFromResult(comparison.getResult(),
                comparison.getInputStringA(), comparison.getInputStringB());

        return buildCompareResponse(comparison, differences);
    }

    @Transactional(readOnly = true)
    public Page<ComparisonListResponse> getUserComparisons(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return comparisonDiffRepository
                .findByCreatedByIdAndDeletedFalseOrderByCreatedAtDesc(userId, pageable)
                .map(this::toListResponse);
    }

    @Transactional
    public CompareResponse recompare(UUID comparisonId, UUID userId) {
        ComparisonDiff comparison = comparisonDiffRepository
                .findByIdAndCreatedByIdAndDeletedFalse(comparisonId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Comparison not found"));

        // Re-parse and compute diff
        JsonNode jsonNodeA = parseJson(comparison.getInputStringA(), "JSON A");
        JsonNode jsonNodeB = parseJson(comparison.getInputStringB(), "JSON B");
        JsonNode diffNode = JsonDiff.asJson(jsonNodeA, jsonNodeB, DIFF_FLAGS);

        List<DiffDetail> differences = parseDiffDetails(diffNode, jsonNodeA, jsonNodeB);
        boolean identical = differences.isEmpty();

        // Update comparison
        comparison.setResult(diffNode.toString());
        comparison.setIdentical(identical);
        comparison.markAsCompared();

        comparison = comparisonDiffRepository.save(comparison);
        log.info("Comparison re-compared: {}", comparison.getId());

        return buildCompareResponse(comparison, differences);
    }

    @Transactional
    public CompareResponse updateDescription(UUID comparisonId, UUID userId, String description) {
        ComparisonDiff comparison = comparisonDiffRepository
                .findByIdAndCreatedByIdAndDeletedFalse(comparisonId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Comparison not found"));

        comparison.setDescription(description);
        comparison = comparisonDiffRepository.save(comparison);

        List<DiffDetail> differences = parseDiffDetailsFromResult(comparison.getResult(),
                comparison.getInputStringA(), comparison.getInputStringB());

        return buildCompareResponse(comparison, differences);
    }

    @Transactional
    public void deleteComparison(UUID comparisonId, UUID userId) {
        ComparisonDiff comparison = comparisonDiffRepository
                .findByIdAndCreatedByIdAndDeletedFalse(comparisonId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Comparison not found"));

        comparison.softDelete();
        comparisonDiffRepository.save(comparison);
        log.info("Comparison soft deleted: {}", comparisonId);
    }

    // ==================== Helper Methods ====================

    private JsonNode parseJson(String json, String fieldName) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(fieldName + " is not valid JSON: " + e.getMessage());
        }
    }

    private List<DiffDetail> parseDiffDetails(JsonNode diffNode, JsonNode sourceA, JsonNode sourceB) {
        List<DiffDetail> details = new ArrayList<>();

        if (diffNode.isArray()) {
            for (JsonNode op : diffNode) {
                DiffDetail detail = DiffDetail.builder()
                        .operation(op.has("op") ? op.get("op").asText() : "unknown")
                        .path(op.has("path") ? op.get("path").asText() : "")
                        .fromValue(op.has("fromValue") ? nodeToObject(op.get("fromValue")) : null)
                        .toValue(op.has("value") ? nodeToObject(op.get("value")) : null)
                        .build();
                details.add(detail);
            }
        }

        return details;
    }

    private List<DiffDetail> parseDiffDetailsFromResult(String resultJson, String jsonA, String jsonB) {
        try {
            JsonNode diffNode = objectMapper.readTree(resultJson);
            JsonNode sourceA = objectMapper.readTree(jsonA);
            JsonNode sourceB = objectMapper.readTree(jsonB);
            return parseDiffDetails(diffNode, sourceA, sourceB);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse stored diff result: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private Object nodeToObject(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        if (node.isNumber()) {
            return node.numberValue();
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        // For arrays and objects, return as string representation
        return node.toString();
    }

    private CompareResponse buildCompareResponse(ComparisonDiff comparison, List<DiffDetail> differences) {
        return CompareResponse.builder()
                .id(comparison.getId())
                .jsonA(comparison.getInputStringA())
                .jsonB(comparison.getInputStringB())
                .identical(comparison.getIdentical())
                .differences(differences)
                .rawDiff(comparison.getResult())
                .diffCount(differences.size())
                .status(comparison.getComparisonStatus())
                .description(comparison.getDescription())
                .createdAt(comparison.getCreatedAt())
                .lastComparedAt(comparison.getLastComparedAt())
                .build();
    }

    private ComparisonListResponse toListResponse(ComparisonDiff comparison) {
        return ComparisonListResponse.builder()
                .id(comparison.getId())
                .identical(comparison.getIdentical())
                .diffCount(countDifferences(comparison.getResult()))
                .status(comparison.getComparisonStatus())
                .description(comparison.getDescription())
                .createdAt(comparison.getCreatedAt())
                .lastComparedAt(comparison.getLastComparedAt())
                .jsonAPreview(truncate(comparison.getInputStringA(), PREVIEW_LENGTH))
                .jsonBPreview(truncate(comparison.getInputStringB(), PREVIEW_LENGTH))
                .build();
    }

    private int countDifferences(String resultJson) {
        try {
            JsonNode diffNode = objectMapper.readTree(resultJson);
            return diffNode.isArray() ? diffNode.size() : 0;
        } catch (JsonProcessingException e) {
            return 0;
        }
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return null;
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength) + "...";
    }
}

