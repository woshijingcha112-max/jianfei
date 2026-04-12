param(
    [string]$DeviceSerial = "",
    [int]$BackendPort = 8080,
    [switch]$SkipBuild,
    [switch]$Launch,
    [switch]$StartBackend
)

$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path $PSScriptRoot -Parent
$adb = Join-Path $projectRoot 'tools\android-sdk\platform-tools\adb.exe'
$apk = Join-Path $projectRoot 'android\app\build\outputs\apk\debug\app-debug.apk'
$gradle = Join-Path $projectRoot 'android\gradlew.bat'
$localJdk = Join-Path $projectRoot 'tools\jdk\current'
$androidSdk = Join-Path $projectRoot 'tools\android-sdk'
$packageName = 'com.dietrecord.app'
$backendScript = Join-Path $projectRoot 'scripts\backend-mvn.ps1'
$backendStartCommand = "powershell -ExecutionPolicy Bypass -File `"$backendScript`" spring-boot:run"

if (-not (Test-Path $adb)) {
    throw "adb not found: $adb"
}

Push-Location $projectRoot
try {
    Write-Host "Backend start command:"
    Write-Host $backendStartCommand

    if ($StartBackend) {
        Write-Host "Start backend in a new PowerShell window..."
        Start-Process `
            -FilePath 'C:\Windows\System32\WindowsPowerShell\v1.0\powershell.exe' `
            -ArgumentList @('-NoExit', '-ExecutionPolicy', 'Bypass', '-File', $backendScript, 'spring-boot:run') `
            -WorkingDirectory $projectRoot
    }

    if (-not $SkipBuild) {
        Write-Host "Build Android debug APK..."
        $env:JAVA_HOME = $localJdk
        $env:PATH = "$localJdk\bin;$env:PATH"
        $env:ANDROID_HOME = $androidSdk
        $env:ANDROID_SDK_ROOT = $androidSdk
        & $gradle ':app:assembleDebug'
    }

    if (-not (Test-Path $apk)) {
        throw "APK not found: $apk"
    }

    $deviceLines = & $adb devices | Select-Object -Skip 1 | Where-Object { $_ -match '\sdevice$' }
    if (-not $DeviceSerial) {
        $tcpDevice = $deviceLines | Where-Object { $_ -match '^\d{1,3}(\.\d{1,3}){3}:\d+\s+device$' } | Select-Object -First 1
        $firstDevice = $deviceLines | Select-Object -First 1
        $selectedLine = if ($tcpDevice) { $tcpDevice } else { $firstDevice }
        if (-not $selectedLine) {
            throw "No Android device found. Pair wireless debugging or connect USB debugging first."
        }
        $DeviceSerial = ($selectedLine -split '\s+')[0]
    }

    Write-Host "Device: $DeviceSerial"
    Write-Host "Install APK: $apk"
    & $adb -s $DeviceSerial install --no-streaming -r $apk

    Write-Host "Setup adb reverse: device 127.0.0.1:$BackendPort -> host 127.0.0.1:$BackendPort"
    & $adb -s $DeviceSerial reverse "tcp:$BackendPort" "tcp:$BackendPort"
    & $adb -s $DeviceSerial reverse --list

    if ($Launch) {
        Write-Host "Launch app: $packageName"
        & $adb -s $DeviceSerial shell monkey -p $packageName -c android.intent.category.LAUNCHER 1
    }
} finally {
    Pop-Location
}
