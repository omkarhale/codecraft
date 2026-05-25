package com.codecraft.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecutionResult {

    private String jobId;
    private ExecutionStatus status;
    private String stdout;
    private String stderr;
    private String compileOutput;
    private Double executionTime;
    private Integer memoryUsed;
    private Language language;
    private long finishedAt;

    public enum ExecutionStatus {
        QUEUED,
        RUNNING,
        SUCCESS,
        COMPILE_ERROR,
        RUNTIME_ERROR,
        TIME_LIMIT,
        MEMORY_LIMIT,
        UNKNOWN_ERROR
    }
}
