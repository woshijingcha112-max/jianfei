# 安卓端骨架说明

## 当前范围

- Kotlin + Jetpack Compose 工程骨架
- Navigation Compose 导航宿主
- 主题与基础视觉变量
- Retrofit + OkHttp 网络层接口声明
- 首页、拍照页、识别结果页、搜索页、目标页占位

## 运行前提

- JDK 17
- Android SDK Platform 36
- Build-Tools 36.0.0
- 本地 Gradle 9.3.1 或可用的 Wrapper

## 首次准备

```powershell
..\scripts\download-dev-env.ps1
..\scripts\bootstrap-jdk17.ps1
..\scripts\bootstrap-gradle.ps1
..\scripts\bootstrap-android-sdk.ps1
```

然后把 `android/local.properties.example` 复制为 `android/local.properties` 并改成实际 SDK 路径。
