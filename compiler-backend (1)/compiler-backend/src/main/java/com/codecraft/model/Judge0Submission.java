package com.codecraft.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Judge0Submission {

    @JsonProperty("source_code")
    private String sourceCode;

    @JsonProperty("language_id")
    private int languageId;

    private String stdin;

    @JsonProperty("cpu_time_limit")
    private double cpuTimeLimit = 5.0;

    @JsonProperty("memory_limit")
    private int memoryLimit = 65536;

    @JsonProperty("wall_time_limit")
    private double wallTimeLimit = 10.0;
}
