package com.codecraft.service;

import com.codecraft.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiService {

    @Qualifier("geminiClient")
    private final WebClient geminiClient;

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    public AiResponse process(AiRequest request) {
        String prompt = buildPrompt(request);
        log.debug("AI {} request for language {}", request.getAction(), request.getLanguage());

        // Gemini request body format (different from Claude)
        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                ),
                "generationConfig", Map.of(
                        "maxOutputTokens", 1024,
                        "temperature", 0.3
                )
        );

        // Gemini URL includes the API key as query param
        String uri = "/v1beta/models/" + model + ":generateContent?key=" + apiKey;

        Map response = geminiClient.post()
                .uri(uri)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        String result = extractText(response);

        return AiResponse.builder()
                .result(result)
                .action(request.getAction())
                .build();
    }

    // Prompts stay exactly the same — copy from your original AiService
    private String buildPrompt(AiRequest req) {
        String lang = req.getLanguage().displayName;
        String code = req.getCode();

        return switch (req.getAction()) {
            case EXPLAIN -> """
                You are a helpful coding tutor. Explain this %s code clearly and concisely.
                Break it down step by step. Use simple language.

```%s
                %s
```

                Explain what this code does, how it works, and any key concepts used.
                Keep the explanation under 300 words.
                """.formatted(lang, req.getLanguage().monacoId, code);

            case FIX -> """
                You are an expert %s developer. The following code has an error.

                Code:
```%s
                %s
```

                Error:
                %s

                Provide:
                1. What caused the error (1-2 sentences)
                2. The fixed code (complete, ready to run)
                3. What you changed (bullet points)

                Format the fixed code in a ```%s code block.
                """.formatted(lang, req.getLanguage().monacoId, code,
                    req.getErrorMessage() != null ? req.getErrorMessage() : "Unknown error",
                    req.getLanguage().monacoId);

            case COMPLEXITY -> """
                Analyze the time and space complexity of this %s code.

```%s
                %s
```

                Provide:
                1. **Time complexity**: Big-O notation with explanation
                2. **Space complexity**: Big-O notation with explanation
                3. **Bottlenecks**: Any performance concerns
                4. **Optimization tip**: One suggestion if applicable

                Be concise and technical.
                """.formatted(lang, req.getLanguage().monacoId, code);

            case GENERATE -> """
                Generate a clean, well-commented %s starter template.
                The user's code context: %s

                Requirements:
                - Runnable immediately, no external dependencies
                - Include comments explaining key parts
                - Follow %s best practices
                - Keep it practical and educational

                Return only the code in a ```%s block, nothing else.
                """.formatted(lang, code.isBlank() ? "general purpose" : code,
                    lang, req.getLanguage().monacoId);
        };
    }

    // Gemini response structure is different from Claude — update the extractor
    @SuppressWarnings("unchecked")
    private String extractText(Map response) {
        try {
            // Gemini: response → candidates[0] → content → parts[0] → text
            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>) response.get("candidates");

            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content =
                        (Map<String, Object>) candidates.get(0).get("content");

                List<Map<String, Object>> parts =
                        (List<Map<String, Object>>) content.get("parts");

                if (parts != null && !parts.isEmpty()) {
                    return (String) parts.get(0).get("text");
                }
            }
        } catch (Exception e) {
            log.error("Failed to extract Gemini response: {}", e.getMessage());
        }
        return "AI response unavailable. Please try again.";
    }
}