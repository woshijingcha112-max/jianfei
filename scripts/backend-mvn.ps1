param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$MavenArgs
)

$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path $PSScriptRoot -Parent
$jdkHome = Join-Path $projectRoot 'tools\\jdk\\current'

if (-not (Test-Path (Join-Path $jdkHome 'bin\\java.exe'))) {
    throw "Local JDK 17 not found: $jdkHome. Run scripts\\bootstrap-jdk17.ps1 first."
}

$env:JAVA_HOME = $jdkHome
$env:Path = "$env:JAVA_HOME\\bin;$env:Path"

if (-not $MavenArgs -or $MavenArgs.Count -eq 0) {
    $MavenArgs = @('-version')
}

Push-Location (Join-Path $projectRoot 'backend')
try {
    & mvn @MavenArgs
} finally {
    Pop-Location
}
