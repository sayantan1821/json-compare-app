package com.jsoncompare.dto.compare;

import com.jsoncompare.model.enums.ComparisonStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompareResponse {

    private UUID id;
    private String jsonA;
    private String jsonB;
    private boolean identical;
    private List<DiffDetail> differences;
    private String rawDiff;
    private int diffCount;
    private ComparisonStatus status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime lastComparedAt;
}

