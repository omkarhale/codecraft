package com.codecraft.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiRequest {

    @NotBlank
    @Size(max = 50_000)
    private String code;

    @NotNull
    private Language language;

    @NotNull
    private AiAction action;

    private String errorMessage;

    public enum AiAction {
        EXPLAIN,
        FIX,
        COMPLEXITY,
        GENERATE
    }
}
