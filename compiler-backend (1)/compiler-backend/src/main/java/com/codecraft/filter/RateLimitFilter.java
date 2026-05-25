package com.codecraft.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiter keyed by IP address.
 * No login required — limits are per-IP only.
 *
 * /api/run  → 10 requests/minute
 * /api/ai/* → 5 requests/minute
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${rate-limit.runs-per-minute:10}")
    private int runsPerMinute;

    @Value("${rate-limit.ai-per-minute:5}")
    private int aiPerMinute;

    // In-memory buckets per IP. Fine for single-instance.
    // For multi-instance deploy, swap this with Redis-backed Bucket4j.
    private final Map<String, Bucket> runBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> aiBuckets  = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        String path = req.getRequestURI();
        String ip   = extractIp(req);

        if (path.startsWith("/api/run")) {
            if (!tryConsume(runBuckets, ip, runsPerMinute)) {
                sendRateLimitError(res, "Too many runs. Max " + runsPerMinute + " per minute.");
                return;
            }
        } else if (path.startsWith("/api/ai")) {
            if (!tryConsume(aiBuckets, ip, aiPerMinute)) {
                sendRateLimitError(res, "Too many AI requests. Max " + aiPerMinute + " per minute.");
                return;
            }
        }

        chain.doFilter(req, res);
    }

    private boolean tryConsume(Map<String, Bucket> buckets, String ip, int limit) {
        Bucket bucket = buckets.computeIfAbsent(ip, k -> buildBucket(limit));
        return bucket.tryConsume(1);
    }

    private Bucket buildBucket(int requestsPerMinute) {
        Bandwidth limit = Bandwidth.classic(
            requestsPerMinute,
            Refill.greedy(requestsPerMinute, Duration.ofMinutes(1))
        );
        return Bucket.builder().addLimit(limit).build();
    }

    private String extractIp(HttpServletRequest req) {
        // Respect X-Forwarded-For when behind a proxy/load balancer
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }

    private void sendRateLimitError(HttpServletResponse res, String message) throws IOException {
        res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        res.setContentType("application/json");
        res.getWriter().write("""
            {"error": "%s", "status": 429}
            """.formatted(message));
    }
}
