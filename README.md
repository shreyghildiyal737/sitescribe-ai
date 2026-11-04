# SiteScribe AI

A lean construction workflow wrapper for Irish site teams.

Core MVP:

```txt
Worker/Site Manager -> voice note + location -> AI draft report -> manager review -> export
```

This is not a full construction management system. It is a wrapper layer that helps teams create clean paperwork from messy site inputs.

## Stack

### Backend
- Java 21
- Spring Boot 3
- Spring Data JPA
- PostgreSQL-ready
- H2 local fallback
- Flyway migrations
- OpenAPI/Swagger

### Frontend
- Next.js
- TypeScript
- TailwindCSS
- Mobile-first PWA-style UI

## Local Setup

### Backend

```bash
cd backend
mvn spring-boot:run
```

API runs at:

```txt
http://localhost:8090
```

Swagger:

```txt
http://localhost:8090/swagger-ui.html
```

By default it uses a local H2 file database at `backend/data/sitescribe`.

`backend/start.ps1` runs it with live AI and the API docs switched on. It reads the Groq and
Gemini keys from `backend/secrets.local.ps1`, which is gitignored; without that file it runs in
mock mode.

For PostgreSQL/Supabase, set:

```env
DATABASE_URL=jdbc:postgresql://host:5432/db
DATABASE_USER=your_user
DATABASE_PASSWORD=your_password
DATABASE_DRIVER=org.postgresql.Driver
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at:

```txt
http://localhost:8091
```

Optional env:

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8090/api
```

## First Demo Flow

1. Open `/login`.
2. Create a new company.
3. Create a job.
4. Open the job.
5. Upload a voice file.
6. Paste a transcript in the optional transcript box for the mock AI MVP.
7. Generate report.
8. Edit, approve, and export text.

## Security Configuration

| Variable | Default | Notes |
|---|---|---|
| `JWT_SECRET` | unset | 32+ bytes, or startup is refused. Unset means a random key per process, so sessions end on restart; set it in production. Generate one with `node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"` |
| `API_DOCS_ENABLED` | `false` | Swagger UI and `/v3/api-docs` are off unless this is `true` |
| `AUTH_RATE_LIMIT_PER_MINUTE` | `10` | Login and sign-up attempts per client address per minute; 429 beyond it. In-memory, per instance |
| `ALLOWED_ORIGINS` | `http://localhost:8091` | Comma-separated CORS origins |

Login answers "Invalid email or password." for both an unknown email and a wrong password, and
runs a bcrypt comparison either way so the response time does not tell them apart.

## Current AI Behaviour

The backend ships with two interchangeable AI paths, selected by env var:

- **Mock (default):** `MockTranscriptionService` + `MockReportExtractionService` — the full workflow runs with no paid APIs.
- **Live:** set `AI_MODE=live` with `GROQ_API_KEY` (Groq Whisper `whisper-large-v3-turbo` transcription) and `GEMINI_API_KEY` (Gemini `gemini-flash-latest` report extraction).

The adapter interfaces are `TranscriptionService` and `ReportExtractionService` under `backend/src/main/java/com/sitescribe/ai/`.

## Product Rules

- AI drafts only.
- Human approval required.
- No continuous worker tracking.
- Location is captured only at submission time when allowed.
- Reports store raw transcript, structured JSON, and final text.
- Gaeilge/mixed-language fields are supported at the data model level.

## MVP Boundaries

Included:
- company setup
- project creation
- worker/manager-ready model
- audio uploads
- location metadata
- mock AI report generation
- report review
- approval
- text export

Also built (beyond the original MVP):
- real BCrypt + JWT auth, per-company data isolation
- live transcription/extraction (Groq Whisper + Gemini) behind `AI_MODE=live`
- storage abstraction (local or Supabase)
- email notifications (Resend) and Sentry error monitoring

Not included yet:
- payments (Stripe — deferred until first paying customer)
- WhatsApp API
- Gmail API
- tender parsing
- OCR
- full offline sync
