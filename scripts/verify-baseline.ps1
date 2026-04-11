param(
    [switch]$Backend,
    [switch]$Android
)

$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path $PSScriptRoot -Parent

if (-not $Backend -and -not $Android) {
    $Backend = $true
    $Android = $true
}

Push-Location $projectRoot
try {
    if ($Backend) {
        & (Join-Path $projectRoot 'scripts\backend-mvn.ps1') -DskipTests package
    }

    if ($Android) {
        & (Join-Path $projectRoot 'android\gradlew.bat') ':app:assembleDebug'
    }
} finally {
    Pop-Location
}
