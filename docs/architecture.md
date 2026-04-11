# 架构设计说明

## 总体方案

仓库按单仓模式拆为两个顶层工程：

- `backend/`：承载 Java 17 + Spring Boot 3 + MyBatis-Plus 后端服务
- `android/`：承载 Kotlin + Jetpack Compose + MVVM 安卓客户端

本次只完成可扩展的基础结构，不落业务实现。

## 后端分层

后端采用按模块聚合的分层结构：

```text
common/     通用返回、异常、基础 DTO
config/     Web、MyBatis-Plus、跨域等基础配置
modules/
  system/   系统探活
  photo/    拍照识别占位
  diet/     饮食记录与统计占位
  food/     食物搜索与详情占位
  goal/     目标设置占位
  weight/   体重记录占位
  period/   生理期留口占位
resources/
  db/       建表 SQL
```

约束说明：

- 保持统一返回结构 `{ code, msg, data }`
- 除上传接口外统一使用 `POST`
- 固定单用户场景，后续统一按 `user_id = 1` 演进
- 数据源、图片存储、百度 AI 参数全部通过环境变量注入

## 安卓端分层

安卓端按“基础能力层 + 功能层”组织：

```text
core/
  data/       调度器与后续仓储基础
  network/    Retrofit 配置、接口声明、网络模型
  ui/theme/   Compose 主题
navigation/   页面路由与导航宿主
feature/
  home/
  camera/
  recognize/
  search/
  goal/
```

当前保留的设计点：

- UI 使用 Jetpack Compose
- 状态层按 MVVM 方向预留，页面先以占位状态驱动
- 网络层先固定 Retrofit + OkHttp + Gson
- 后续可在 `feature/*` 中分别补 ViewModel、Repository 和业务状态
