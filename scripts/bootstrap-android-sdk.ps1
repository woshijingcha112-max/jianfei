param(
    [string]$DownloadRoot = (Join-Path $PSScriptRoot '..\\tools\\downloads'),
    [string]$SdkRoot = (Join-Path $PSScriptRoot '..\\tools\\android-sdk')
)

$ErrorActionPreference = 'Stop'

$zipPath = Join-Path $DownloadRoot 'commandlinetools-win-14742923_latest.zip'
if (-not (Test-Path $zipPath)) {
    throw "Command-line tools zip not found: $zipPath"
}

$extractRoot = Join-Path $SdkRoot 'cmdline-tools'
$latestRoot = Join-Path $extractRoot 'latest'

New-Item -ItemType Directory -Path $extractRoot -Force | Out-Null

if (Test-Path $latestRoot) {
    Remove-Item -LiteralPath $latestRoot -Recurse -Force
}

Expand-Archive -LiteralPath $zipPath -DestinationPath $extractRoot -Force

$cmdlineSource = Join-Path $extractRoot 'cmdline-tools'
if (-not (Test-Path $cmdlineSource)) {
    throw "Unexpected command-line tools structure: $cmdlineSource"
}

Move-Item -LiteralPath $cmdlineSource -Destination $latestRoot

Write-Host "ANDROID_SDK_ROOT=$SdkRoot"
Write-Host "Next step:"
$sdkManagerHint = '{0}\bin\sdkmanager.bat --sdk_root={1} "platform-tools" "platforms;android-36" "build-tools;36.0.0"' -f $latestRoot, $SdkRoot
Write-Host "  $sdkManagerHint"
