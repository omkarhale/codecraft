package com.codecraft.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CodeRequest {

    @NotBlank(message = "Code cannot be empty")
    @Size(max = 50_000, message = "Code too large (max 50,000 chars)")
    private String code;

    @NotNull(message = "Language is required")
    private Language language;

    @Size(max = 4096, message = "stdin too large")
    private String stdin;
}
