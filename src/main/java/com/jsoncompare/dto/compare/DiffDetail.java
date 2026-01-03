package com.jsoncompare.dto.compare;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiffDetail {

    /**
     * Type of operation: add, remove, replace, move, copy
     */
    private String operation;

    /**
     * JSON path where the difference occurs (e.g., "/users/0/name")
     */
    private String path;

    /**
     * The value in JSON A (for remove/replace operations)
     */
    private Object fromValue;

    /**
     * The value in JSON B (for add/replace operations)
     */
    private Object toValue;
}

