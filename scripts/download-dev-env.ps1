param(
    [string]$DownloadRoot = (Join-Path $PSScriptRoot '..\\tools\\downloads'),
    [switch]$IncludeAndroidStudio
)

$ErrorActionPreference = 'Stop'

New-Item -ItemType Directory -Path $DownloadRoot -Force | Out-Null

$packages = @(
    @{
        Name = 'Temurin JDK 17'
        Url = 'https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse'
        FileName = 'temurin-jdk17-windows-x64.zip'
    },
    @{
        Name = 'Android SDK Command-line Tools'
        Url = 'https://dl.google.com/android/repository/commandlinetools-win-14742923_latest.zip'
        FileName = 'commandlinetools-win-14742923_latest.zip'
    },
    @{
        Name = 'Gradle 9.3.1'
        Url = 'https://services.gradle.org/distributions/gradle-9.3.1-bin.zip'
        FileName = 'gradle-9.3.1-bin.zip'
    }
)

if ($IncludeAndroidStudio) {
    $packages += @{
        Name = 'Android Studio Panda 3'
        Url = 'https://edgedl.me.gvt1.com/android/studio/install/2025.3.3.6/android-studio-panda3-windows.exe'
        FileName = 'android-studio-panda3-windows.exe'
    }
}

foreach ($package in $packages) {
    $target = Join-Path $DownloadRoot $package.FileName
    if (Test-Path $target) {
        Write-Host "[skip] $($package.Name) already exists: $target"
        continue
    }

    Write-Host "[download] $($package.Name)"
    Invoke-WebRequest -Uri $package.Url -OutFile $target
    Write-Host "[done] $target"
}

Write-Host ''
Write-Host 'Downloads completed.'
