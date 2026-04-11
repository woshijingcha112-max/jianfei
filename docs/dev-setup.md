# 开发环境准备

更新时间：2026-04-10

## 当前机器检查结果

- 已安装 `Java 8`：`1.8.0_202`
- 已安装 `Maven`：`3.9.9`
- 未发现 `Android Studio`
- 未发现 `Android SDK`
- 未发现 `adb`
- 未发现 `sdkmanager`

## 本项目最低开发环境

### 后端

- JDK 17
- Maven 3.9+
- MySQL 8.0

### 安卓端

- Android Studio
- Android SDK Command-line Tools
- Android SDK Platform 36
- Android Build-Tools 36.0.0
- Gradle 9.3.1

## 官方下载来源

- Eclipse Temurin JDK 17 发布页：
  [Adoptium Temurin 17](https://adoptium.net/zh-cn/temurin/releases/?arch=x64&os=windows&package=jdk&version=17)
- Android Studio 下载页：
  [Android Studio](https://developer.android.com/studio)
- Android SDK Command-line Tools：
  [Command-line Tools](https://developer.android.com/studio/command-line)
- Gradle 当前文档页：
  [Gradle Getting Started](https://docs.gradle.org/current/userguide/getting_started.html)

## 已在脚本中固定的官方下载地址

- JDK 17 最新 Windows x64 二进制：
  `https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse`
- Android Studio Panda 3 Windows 安装包：
  `https://edgedl.me.gvt1.com/android/studio/install/2025.3.3.6/android-studio-panda3-windows.exe`
- Android SDK Command-line Tools：
  `https://dl.google.com/android/repository/commandlinetools-win-14742923_latest.zip`
- Gradle 9.3.1：
  `https://services.gradle.org/distributions/gradle-9.3.1-bin.zip`
## 建议步骤

1. 先执行 `scripts/download-dev-env.ps1`
2. 执行 `scripts/bootstrap-jdk17.ps1`
3. 执行 `scripts/bootstrap-gradle.ps1`
4. 如只需要先跑后端，继续执行 `scripts/use-local-jdk17.ps1`
5. 如要补安卓命令行环境，执行 `scripts/bootstrap-android-sdk.ps1`
6. 最终再用 Android Studio 打开 `android/`

## 说明

- 当前仓库已经能承载骨架代码，但未安装 JDK 17 前无法编译 Spring Boot 3
- 安卓端骨架已创建，但未补齐 SDK 前无法构建 APK
- Android Studio 安装包较大，脚本默认不自动下载，需加 `-IncludeAndroidStudio`
- 如果本机其他项目仍依赖 `JDK 8`，不要改系统级 `JAVA_HOME`，直接使用 `scripts/backend-mvn.ps1` 与 `android/gradlew.bat`
