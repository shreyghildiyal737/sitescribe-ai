# Implementation Notes

## Backend Design

The backend is intentionally modular but not over-engineered.

Packages:

- `auth`: BCrypt passwords, JWT sessions, per-address rate limit on login and sign-up
- `company`: company tenant root
- `user`: users and roles
- `project`: jobs and assignments
- `upload`: audio/photo uploads and location metadata
- `report`: report lifecycle
- `ai`: transcription/extraction adapters
- `export`: simple export endpoints
- `audit`: future audit trail foundation

## Security Model

Every request outside `/api/auth/**` needs a valid JWT. Company isolation is enforced in the
services: a record belonging to another company answers 404, so its existence does not leak.
`CompanyIsolationTest` and `AuthHardeningTest` guard both. Still open: rate limiting is
in-memory per instance, and sign-up answers 409 for an email already registered.

## Report Lifecycle

```txt
Upload -> Transcript -> Structured JSON -> Draft Report -> Edit -> Approve -> Export
```

## Location Model

Location is optional and submission-scoped only.

Do not build continuous tracking.

## Gaeilge Support

The data model supports:

- `inputLanguage`
- `outputLanguage`
- mixed-language detection

The mock service detects limited Gaeilge terms only. Real support needs a proper speech-to-text and translation model.
