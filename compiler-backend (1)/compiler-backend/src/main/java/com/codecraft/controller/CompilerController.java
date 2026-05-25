package com.codecraft.controller;

import com.codecraft.executor.Judge0ExecutorService;
import com.codecraft.model.*;
import com.codecraft.service.AiService;
import com.codecraft.service.LanguageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * All public API endpoints.
 * No auth — anyone can call these directly.
 *
 * POST /api/run              → submit code, get jobId
 * GET  /api/result/{jobId}  → poll for result
 * POST /api/ai               → AI explain/fix/complexity/generate
 * GET  /api/languages        → list all supported languages + starter code
 * GET  /api/health           → health check
 */
@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class CompilerController {

    private final Judge0ExecutorService executorService;
    private final AiService aiService;
    private final LanguageService languageService;

    /**
     * Submit code for execution.
     * Returns immediately with a jobId.
     * Frontend then either polls GET /api/result/{jobId}
     * OR subscribes to WebSocket /topic/output/{jobId} for real-time push.
     */
//    @PostMapping("/run")
//    public ResponseEntity<SubmitResponse> run(@Valid @RequestBody CodeRequest request) {
//        String jobId = UUID.randomUUID().toString();
//        log.info("New run job {} — language: {}", jobId, request.getLanguage());
//
//        // Fire and forget — result pushed via WebSocket
//        executorService.executeAsync(jobId, request);
//
//        return ResponseEntity.accepted().body(
//            SubmitResponse.builder()
//                .jobId(jobId)
//                .status("queued")
//                .submittedAt(Instant.now().toEpochMilli())
//                .build()
//        );
//    }

    @PostMapping("/run")
    public ResponseEntity<?> run(@RequestBody CodeRequest request) {

        System.out.println("🔥 API HIT");

        String jobId = UUID.randomUUID().toString();

        // ❌ COMMENT THIS LINE
        // executorService.executeAsync(jobId, request);

        return ResponseEntity.ok(Map.of(
                "jobId", jobId,
                "status", "test-working"
        ));
    }

    /**
     * Poll for result by jobId.
     * Returns 404 while still running (frontend keeps polling or uses WebSocket instead).
     */
    @GetMapping("/result/{jobId}")
    public ResponseEntity<ExecutionResult> getResult(@PathVariable String jobId) {
        ExecutionResult result = executorService.getResult(jobId);

        if (result == null) {
            // Still running — return 202 Accepted so frontend knows to keep waiting
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(ExecutionResult.builder()
                            .jobId(jobId)
                            .status(ExecutionResult.ExecutionStatus.QUEUED)
                            .build());
        }
        return ResponseEntity.ok(result);
    }

    /**
     * AI features — explain, fix, complexity analysis, generate.
     * Calls Claude API. Costs ~$0.001 per request on Haiku model.
     */
    @PostMapping("/ai")
    public ResponseEntity<AiResponse> ai(@Valid @RequestBody AiRequest request) {
        log.info("AI {} request for {}", request.getAction(), request.getLanguage());
        AiResponse response = aiService.process(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Returns all supported languages with display names and starter code.
     * Frontend calls this once on load to populate the language picker.
     */
    @GetMapping("/languages")
    public ResponseEntity<List<LanguageInfo>> getLanguages() {
        return ResponseEntity.ok(languageService.getAllLanguages());
    }

    /**
     * Health check — useful for deployment platforms (Railway, Render, Fly.io).
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "ok",
            "service", "codecraft-compiler",
            "timestamp", Instant.now().toString()
        ));
    }
}
