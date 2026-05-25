package com.codecraft.executor;

import com.codecraft.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class Judge0ExecutorService {

    @Qualifier("judge0Client")
    private final WebClient judge0Client;

    private final SimpMessagingTemplate websocket;

    @Value("${judge0.poll-timeout:30000}")
    private long pollTimeoutMs;

    @Value("${judge0.poll-interval:500}")
    private long pollIntervalMs;

    private final Map<String, ExecutionResult> resultStore = new ConcurrentHashMap<>();

    @Async
    public void executeAsync(String jobId, CodeRequest request) {
        log.debug("Executing job {} for language {}", jobId, request.getLanguage());

        try {
            String token = submitToJudge0(request);
            Judge0Result judge0Result = pollUntilDone(token);
            ExecutionResult result = mapResult(jobId, judge0Result, request.getLanguage());

            resultStore.put(jobId, result);
            websocket.convertAndSend("/topic/output/" + jobId, result);

            log.debug("Job {} finished with status {}", jobId, result.getStatus());

        } catch (Exception e) {
            log.error("Job {} failed: {}", jobId, e.getMessage(), e);
            ExecutionResult errorResult = ExecutionResult.builder()
                    .jobId(jobId)
                    .status(ExecutionResult.ExecutionStatus.UNKNOWN_ERROR)
                    .stderr("Internal error: " + e.getMessage())
                    .finishedAt(Instant.now().toEpochMilli())
                    .build();
            resultStore.put(jobId, errorResult);
            websocket.convertAndSend("/topic/output/" + jobId, errorResult);
        }
    }

    public ExecutionResult getResult(String jobId) {
        return resultStore.get(jobId);
    }

    private String submitToJudge0(CodeRequest request) {
        String encoded = Base64.getEncoder().encodeToString(request.getCode().getBytes());
        String encodedStdin = request.getStdin() != null
                ? Base64.getEncoder().encodeToString(request.getStdin().getBytes())
                : null;

        Judge0Submission submission = Judge0Submission.builder()
                .sourceCode(encoded)
                .languageId(request.getLanguage().judge0Id)
                .stdin(encodedStdin)
                .cpuTimeLimit(5.0)
                .memoryLimit(65536)
                .wallTimeLimit(10.0)
                .build();

        JsonNode response = judge0Client.post()
                .uri("/submissions?base64_encoded=true&wait=false")
                .bodyValue(submission)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null || !response.has("token")) {
            throw new RuntimeException("Judge0 did not return a token");
        }
        return response.get("token").asText();
    }

    private Judge0Result pollUntilDone(String token) throws InterruptedException {
        long deadline = System.currentTimeMillis() + pollTimeoutMs;

        while (System.currentTimeMillis() < deadline) {
            Judge0Result result = judge0Client.get()
                    .uri("/submissions/{token}?base64_encoded=true&fields=stdout,stderr,compile_output,status,time,memory", token)
                    .retrieve()
                    .bodyToMono(Judge0Result.class)
                    .block();

            if (result != null && result.getStatus() != null) {
                int statusId = result.getStatus().getId();
                if (statusId >= 3) {
                    log.debug("Judge0 result: status={}, stdout={}, stderr={}, compile={}",
                            statusId, result.getStdout() != null, result.getStderr() != null, result.getCompileOutput() != null);
                    return result;
                }
            }
            Thread.sleep(pollIntervalMs);
        }
        throw new RuntimeException("Execution timed out waiting for Judge0");
    }

    private ExecutionResult mapResult(String jobId, Judge0Result j0, Language language) {
        String stdout = decodeBase64(j0.getStdout());
        String stderr = decodeBase64(j0.getStderr());
        String compileOut = decodeBase64(j0.getCompileOutput());

        ExecutionResult.ExecutionStatus status = mapStatus(j0.getStatus().getId());

        return ExecutionResult.builder()
                .jobId(jobId)
                .status(status)
                .stdout(stdout)
                .stderr(stderr)
                .compileOutput(compileOut)
                .executionTime(j0.getTime())
                .memoryUsed(j0.getMemory())
                .language(language)
                .finishedAt(Instant.now().toEpochMilli())
                .build();
    }

    private String decodeBase64(String encoded) {
        if (encoded == null || encoded.isBlank()) return null;
        try {
            byte[] decoded = Base64.getDecoder().decode(encoded);
            return new String(decoded);
        } catch (IllegalArgumentException e) {
            // Already plain text, not base64
            log.debug("String not base64 encoded, returning as-is: {}", encoded.substring(0, Math.min(50, encoded.length())));
            return encoded;
        }
    }

    private ExecutionResult.ExecutionStatus mapStatus(int judge0StatusId) {
        return switch (judge0StatusId) {
            case 3  -> ExecutionResult.ExecutionStatus.SUCCESS;
            case 4  -> ExecutionResult.ExecutionStatus.TIME_LIMIT;
            case 5  -> ExecutionResult.ExecutionStatus.TIME_LIMIT;
            case 6  -> ExecutionResult.ExecutionStatus.COMPILE_ERROR;
            case 7, 8, 9, 10, 11 -> ExecutionResult.ExecutionStatus.RUNTIME_ERROR;
            case 12 -> ExecutionResult.ExecutionStatus.MEMORY_LIMIT;
            case 13 -> ExecutionResult.ExecutionStatus.UNKNOWN_ERROR;  // ← Add this
            default -> ExecutionResult.ExecutionStatus.UNKNOWN_ERROR;
        };
    }
}