package com.codecraft.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LanguageInfo {
    private String id;
    private String displayName;
    private String monacoId;
    private String starterCode;
}
