# Implementation Notes

## Backend Design

The backend is intentionally modular but not over-engineered.

Packages:

- `auth`: demo bootstrap/login only
- `company`: company tenant root
- `user`: users and roles
- `project`: jobs and assignments
- `upload`: audio/photo uploads and location metadata
- `report`: report lifecycle
- `ai`: transcription/extraction adapters
- `export`: simple export endpoints
- `audit`: future audit trail foundation

## Security Warning

Demo auth is not production authentication. Before production, add proper JWT/session auth and method-level authorization.

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
