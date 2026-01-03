package com.jsoncompare.controller;

import com.jsoncompare.dto.compare.*;
import com.jsoncompare.service.AuthService;
import com.jsoncompare.service.CompareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/compare")
@RequiredArgsConstructor
@Tag(name = "JSON Compare", description = "JSON comparison endpoints")
public class CompareController {

    private final CompareService compareService;
    private final AuthService authService;

    @PostMapping
    @Operation(summary = "Compare two JSON objects")
    public ResponseEntity<CompareResponse> compare(
            @Valid @RequestBody CompareRequest request,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = getUserIdFromToken(authHeader);
        CompareResponse response = compareService.compare(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a specific comparison by ID")
    public ResponseEntity<CompareResponse> getComparison(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = getUserIdFromToken(authHeader);
        CompareResponse response = compareService.getComparison(id, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all comparisons for the current user")
    public ResponseEntity<Page<ComparisonListResponse>> getUserComparisons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = getUserIdFromToken(authHeader);
        Page<ComparisonListResponse> response = compareService.getUserComparisons(userId, page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/recompare")
    @Operation(summary = "Re-run comparison with existing JSON inputs")
    public ResponseEntity<CompareResponse> recompare(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = getUserIdFromToken(authHeader);
        CompareResponse response = compareService.recompare(id, userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update comparison description")
    public ResponseEntity<CompareResponse> updateDescription(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = getUserIdFromToken(authHeader);
        String description = body.get("description");
        CompareResponse response = compareService.updateDescription(id, userId, description);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a comparison (soft delete)")
    public ResponseEntity<Map<String, String>> deleteComparison(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = getUserIdFromToken(authHeader);
        compareService.deleteComparison(id, userId);
        return ResponseEntity.ok(Map.of("message", "Comparison deleted successfully"));
    }

    // ==================== Helper Methods ====================

    private UUID getUserIdFromToken(String authHeader) {
        String token = extractToken(authHeader);
        return authService.getCurrentUser(token).getId();
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new IllegalArgumentException("Authorization header is required");
        }
        if (authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }
}
