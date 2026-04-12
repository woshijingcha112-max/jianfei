param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$MavenArgs
)

$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path $PSScriptRoot -Parent
$jdkHome = Join-Path $projectRoot 'tools\\jdk\\current'
$mavenHome = Join-Path $projectRoot 'tools\\maven\\current'

if (-not (Test-Path (Join-Path $jdkHome 'bin\\java.exe'))) {
    throw "Local JDK 17 not found: $jdkHome. Run scripts\\bootstrap-jdk17.ps1 first."
}

if (-not (Test-Path (Join-Path $mavenHome 'bin\\mvn.cmd'))) {
    throw "Local Maven not found: $mavenHome. Prepare tools\\maven\\current first."
}

$env:JAVA_HOME = $jdkHome
$env:MAVEN_HOME = $mavenHome
$env:Path = "$env:JAVA_HOME\\bin;$env:MAVEN_HOME\\bin;$env:Path"

# 统一 PowerShell 控制台与 Maven/JVM 的输出编码为 UTF-8，避免中文日志乱码。
[Console]::InputEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
$OutputEncoding = [System.Text.UTF8Encoding]::new($false)
$env:MAVEN_OPTS = "-Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 $env:MAVEN_OPTS".Trim()
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 $env:JAVA_TOOL_OPTIONS".Trim()

if (-not $MavenArgs -or $MavenArgs.Count -eq 0) {
    $MavenArgs = @('-version')
}

Push-Location (Join-Path $projectRoot 'backend')
try {
    & mvn @MavenArgs
} finally {
    Pop-Location
}
