# 减肥记录 App

这是一个“拍照识别餐食并记录减肥计划”的 MVP 项目，包含后端服务与安卓客户端。

当前仓库已经不只是基础骨架。首页主链路最小真实化已落地，前后端已接到真实接口；识别结果仍是后端基于种子食物库生成的 mock 结果，尚未接入真实百度 AI。

## 文档入口

- [AGENTS.md](AGENTS.md)：仓库最小执行入口
- [spec.md](spec.md)：当前硬边界与软规范
- [feature_status.md](feature_status.md)：真实完成度真值
- [project_develop_log.md](project_develop_log.md)：每轮开发复盘
- [减肥计划拍照识别软件开发设计文档.md](减肥计划拍照识别软件开发设计文档.md)：原始设计基线，只读
- `scripts/verify-baseline.ps1`：最小机械验证入口

如果 README 与上述文档有冲突，以 `spec.md`、`feature_status.md`、`project_develop_log.md` 为准。

## 当前状态

- 后端：`Spring Boot 3 + MyBatis-Plus + MySQL`
- 安卓端：`Kotlin + Jetpack Compose + Retrofit`
- 当前阶段：首页主链路最小真实化已完成代码落地与后端接口实调，待安卓端手工联调

当前已落地的真实链路：

- `POST /goal/get`
- `POST /goal/save`
- `POST /diet/photo/upload`
- `POST /diet/record/save`
- `POST /diet/record/list`
- `POST /diet/stat/today`

当前已明确未纳入本轮范围：

- `food/search`、`food/detail`
- `diet/record/delete`
- `weight/record`
- `period/*`
- CameraX
- 真实百度 AI 识别
- 人工修正交互

## 仓库结构

```text
backend/    Spring Boot 后端
android/    Android App（Jetpack Compose）
docs/       环境与架构说明
scripts/    本地开发与验证脚本
```

## 后端实现概览

- 单数据源 + MyBatis-Plus 已恢复启用
- 最小表基线已落地：`user_profile`、`diet_record`、`diet_item`、`food_library`
- 当前按单用户 MVP 约束处理，固定 `user_id = 1`
- 图片上传支持落盘与静态访问映射
- 识别结果当前由后端根据 `food_library` 种子数据做 mock 匹配生成

## 安卓端实现概览

- 首页、拍照、识别结果、目标页已切到真实 repository
- 首页总览固定来自 `/diet/stat/today`
- 拍照页当前上传内置示例资源 `sample_meal.png`，不接 CameraX
- 识别结果确认保存后，会通过真实接口刷新首页
- 搜索页仍为占位页面，不接主链路

## 开发与启动

建议顺序：

1. 先读 [spec.md](spec.md) 与 [feature_status.md](feature_status.md)
2. 阅读 [docs/dev-setup.md](docs/dev-setup.md)
3. 执行 `scripts/download-dev-env.ps1`
4. 执行 `scripts/bootstrap-jdk17.ps1`
5. 执行 `scripts/bootstrap-gradle.ps1`
6. 后端优先使用 `scripts/backend-mvn.ps1`
7. 安卓端使用 `android/gradlew.bat` 或 Android Studio

如果本机其他项目仍依赖 `JDK 8`，优先使用项目脚本做局部切换，不要覆盖系统默认 JDK。

## 验证

最小机械验证：

- `scripts/verify-baseline.ps1`

当前文档记录的最近验证结果包括：

- 安卓 `:app:assembleDebug` 通过
- 后端 compile 通过
- 后端 6 个真实接口 smoke test 已通过

## 当前下一步

按当前边界，下一步应在安卓模拟器或真机上完成一次手工联调：

`首页 -> 拍照 -> 识别结果 -> 保存 -> 首页刷新`

若继续扩能力，再进入真实 AI、搜索或体重记录。
