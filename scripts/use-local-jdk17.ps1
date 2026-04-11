param(
    [string]$JdkRoot = (Join-Path $PSScriptRoot '..\\tools\\jdk')
)

$ErrorActionPreference = 'Stop'

if (-not (Test-Path $JdkRoot)) {
    throw "JDK root not found: $JdkRoot"
}

$currentHome = Join-Path $JdkRoot 'current'
if (Test-Path (Join-Path $currentHome 'bin\\java.exe')) {
    $jdkHome = Get-Item -LiteralPath $currentHome
} else {
$jdkHome = Get-ChildItem -Path $JdkRoot -Directory |
    Where-Object { $_.Name -like 'jdk-*' -or $_.Name -like 'OpenJDK*' } |
    Sort-Object Name -Descending |
    Select-Object -First 1
}

if (-not $jdkHome) {
    throw "No extracted JDK 17 directory found under $JdkRoot"
}

$env:JAVA_HOME = $jdkHome.FullName
$env:Path = "$($env:JAVA_HOME)\\bin;$env:Path"

Write-Host "JAVA_HOME=$env:JAVA_HOME"
& java -version
