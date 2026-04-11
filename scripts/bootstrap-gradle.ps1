param(
    [string]$DownloadRoot = (Join-Path $PSScriptRoot '..\\tools\\downloads'),
    [string]$GradleRoot = (Join-Path $PSScriptRoot '..\\tools\\gradle')
)

$ErrorActionPreference = 'Stop'

$zipPath = Join-Path $DownloadRoot 'gradle-9.3.1-bin.zip'
if (-not (Test-Path $zipPath)) {
    throw "Gradle zip not found: $zipPath"
}

New-Item -ItemType Directory -Path $GradleRoot -Force | Out-Null

$currentRoot = Join-Path $GradleRoot 'current'
if (Test-Path $currentRoot) {
    Remove-Item -LiteralPath $currentRoot -Recurse -Force
}

$extractRoot = Join-Path $GradleRoot 'extracted'
if (Test-Path $extractRoot) {
    Remove-Item -LiteralPath $extractRoot -Recurse -Force
}

Expand-Archive -LiteralPath $zipPath -DestinationPath $extractRoot -Force

$gradleHome = Get-ChildItem -Path $extractRoot -Directory | Select-Object -First 1
if (-not $gradleHome) {
    throw "No Gradle directory found after extraction"
}

Move-Item -LiteralPath $gradleHome.FullName -Destination $currentRoot
Remove-Item -LiteralPath $extractRoot -Recurse -Force

Write-Host "Gradle extracted to $currentRoot"
