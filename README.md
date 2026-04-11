# 减肥记录 App 基础架构

当前仓库只落前后端基础骨架与本地开发环境准备脚本，不包含业务逻辑实现。

## Harness 入口

- [AGENTS.md](AGENTS.md)：最小入口与执行顺序
- [spec.md](spec.md)：当前边界协议
- [feature_status.md](feature_status.md)：真实完成度真值
- [project_develop_log.md](project_develop_log.md)：每轮复盘
- [减肥计划拍照识别软件开发设计文档.md](减肥计划拍照识别软件开发设计文档.md)：原始设计基线，只读
- `scripts/verify-baseline.ps1`：最小机械验证

## 仓库结构

```text
backend/    Spring Boot 3 + MyBatis-Plus 后端骨架
android/    Kotlin + Jetpack Compose 安卓端骨架
docs/       架构说明与环境准备说明
scripts/    开发环境下载与本地引导脚本
tools/      本地下载产物与工具目录（默认忽略）
```

## 当前完成范围

- 后端基础工程、统一返回结构、全局异常、配置骨架、接口占位路由
- 安卓基础工程、Compose 导航骨架、主题、网络层接口定义、页面占位
- 开发环境缺口说明、官方软件下载脚本、本地 JDK 17 启用脚本

## 建议启动顺序

1. 先读 [spec.md](spec.md) 和 [feature_status.md](feature_status.md)
2. 阅读 [开发环境说明](docs/dev-setup.md)
3. 执行 `scripts/download-dev-env.ps1`
4. 执行 `scripts/bootstrap-jdk17.ps1`
5. 执行 `scripts/bootstrap-gradle.ps1`
6. 如需先编译后端，执行 `scripts/use-local-jdk17.ps1`
7. 进入 `backend/` 补齐数据库并开始业务开发
8. 用 Android Studio 打开 `android/` 继续前端业务实现

如果你本机其他项目依赖 `JDK 8`，优先使用 `scripts/backend-mvn.ps1` 和 `android/gradlew.bat`，它们只对当前项目临时切换到仓库内的 `JDK 17`。
