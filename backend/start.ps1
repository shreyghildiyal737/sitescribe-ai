# Runs the backend with live AI. Keys live in secrets.local.ps1 (gitignored), which sets
# $env:GROQ_API_KEY and $env:GEMINI_API_KEY. Without it the backend falls back to mock mode.
$secrets = Join-Path $PSScriptRoot "secrets.local.ps1"
if (Test-Path $secrets) {
    . $secrets
    $env:AI_MODE = "live"
} else {
    Write-Warning "secrets.local.ps1 not found - running in mock AI mode."
}
$env:API_DOCS_ENABLED = "true"
mvn spring-boot:run
