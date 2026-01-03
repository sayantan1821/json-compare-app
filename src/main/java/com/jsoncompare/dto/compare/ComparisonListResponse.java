package com.jsoncompare.dto.compare;

import com.jsoncompare.model.enums.ComparisonStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonListResponse {

    private UUID id;
    private boolean identical;
    private int diffCount;
    private ComparisonStatus status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime lastComparedAt;

    // Preview of JSON inputs (truncated)
    private String jsonAPreview;
    private String jsonBPreview;
}

