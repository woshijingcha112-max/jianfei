param(
    [string]$DownloadRoot = (Join-Path $PSScriptRoot '..\\tools\\downloads'),
    [string]$JdkRoot = (Join-Path $PSScriptRoot '..\\tools\\jdk')
)

$ErrorActionPreference = 'Stop'

$zipPath = Join-Path $DownloadRoot 'temurin-jdk17-windows-x64.zip'
if (-not (Test-Path $zipPath)) {
    throw "JDK zip not found: $zipPath"
}

New-Item -ItemType Directory -Path $JdkRoot -Force | Out-Null

$currentRoot = Join-Path $JdkRoot 'current'
if (Test-Path $currentRoot) {
    Remove-Item -LiteralPath $currentRoot -Recurse -Force
}

$extractRoot = Join-Path $JdkRoot 'extracted'
if (Test-Path $extractRoot) {
    Remove-Item -LiteralPath $extractRoot -Recurse -Force
}

Expand-Archive -LiteralPath $zipPath -DestinationPath $extractRoot -Force

$jdkHome = Get-ChildItem -Path $extractRoot -Directory | Select-Object -First 1
if (-not $jdkHome) {
    throw "No JDK directory found after extraction"
}

Move-Item -LiteralPath $jdkHome.FullName -Destination $currentRoot
Remove-Item -LiteralPath $extractRoot -Recurse -Force

Write-Host "JDK extracted to $currentRoot"
