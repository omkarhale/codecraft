# CodeCraft Compiler — Backend

## Quick start (local)

### Prerequisites
- Docker + Docker Compose
- Java 21 (for running without Docker)

### 1. Clone and set up environment
```bash
cp .env.example .env
# Edit .env and add your ANTHROPIC_API_KEY
```

### 2. Start everything with Docker Compose
```bash
docker-compose up -d
```

Wait ~30 seconds for Judge0 to initialize, then:

```bash
# Check health
curl http://localhost:8080/api/health

# Test code execution
curl -X POST http://localhost:8080/api/run \
  -H "Content-Type: application/json" \
  -d '{"code": "print(\"Hello World\")", "language": "PYTHON"}'

# Response: {"jobId":"abc-123","status":"queued","submittedAt":...}

# Poll for result
curl http://localhost:8080/api/result/abc-123

# Test AI explain
curl -X POST http://localhost:8080/api/ai \
  -H "Content-Type: application/json" \
  -d '{"code": "def fib(n): return n if n<=1 else fib(n-1)+fib(n-2)", "language": "PYTHON", "action": "EXPLAIN"}'
```

### 3. Run without Docker (dev mode)
```bash
# Start only Redis and Judge0
docker-compose up -d redis judge0-server judge0-workers judge0-db judge0-redis

# Run Spring Boot
./mvnw spring-boot:run
```

---

## API Reference

| Method | Endpoint | Body | Description |
|--------|----------|------|-------------|
| POST | `/api/run` | `{code, language, stdin?}` | Submit code, returns `jobId` |
| GET | `/api/result/{jobId}` | — | Poll for result |
| POST | `/api/ai` | `{code, language, action, errorMessage?}` | AI features |
| GET | `/api/languages` | — | List all languages + starter code |
| GET | `/api/health` | — | Health check |

**Languages:** `PYTHON`, `JAVASCRIPT`, `JAVA`, `CPP`, `C`, `GO`, `RUST`, `PHP`, `RUBY`, `KOTLIN`, `TYPESCRIPT`, `SWIFT`, `R`, `BASH`

**AI actions:** `EXPLAIN`, `FIX`, `COMPLEXITY`, `GENERATE`

**WebSocket:** Connect to `ws://localhost:8080/ws`, subscribe to `/topic/output/{jobId}` for real-time results.

---

## Deploy to Railway (free tier)

```bash
# Install Railway CLI
npm i -g @railway/cli

railway login
railway init
railway add --database redis   # adds Redis automatically

# Set env vars
railway variables set ANTHROPIC_API_KEY=your-key-here
railway variables set JUDGE0_URL=http://your-judge0-instance.com

railway up
```

**Note:** Judge0 needs a separate server (min 1GB RAM). Deploy it on Fly.io free tier:
```bash
# In the judge0 directory
flyctl launch --image judge0/judge0:1.13.1
```

---

## Project structure

```
src/main/java/com/codecraft/
├── CompilerApplication.java      # entry point
├── controller/
│   ├── CompilerController.java   # all REST endpoints
│   └── GlobalExceptionHandler.java
├── executor/
│   └── Judge0ExecutorService.java  # runs code via Judge0
├── service/
│   ├── AiService.java            # Claude API calls
│   └── LanguageService.java      # language metadata + starter code
├── model/
│   └── Models.java               # all request/response classes
├── filter/
│   └── RateLimitFilter.java      # IP-based rate limiting
└── config/
    └── AppConfig.java            # CORS, WebSocket, WebClient beans
```
