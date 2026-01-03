package com.jsoncompare.dto.compare;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompareRequest {

    @NotBlank(message = "JSON A is required")
    private String jsonA;

    @NotBlank(message = "JSON B is required")
    private String jsonB;

    private String description;
}

