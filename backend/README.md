# 后端骨架说明

## 当前范围

- Spring Boot 3.x 工程
- 统一返回结构
- 全局异常处理
- MyBatis-Plus 基础配置
- 各业务接口占位 Controller
- 建表 SQL

当前为了让空库阶段也能先起服务，临时排除了数据源与 MyBatis-Plus 自动装配。进入业务开发阶段时，需要恢复自动装配并接入真实数据源。

## 运行前提

- JDK 17
- MySQL 8

## 推荐启动方式

```powershell
cd backend
mvn spring-boot:run
```

如果机器仍默认使用 Java 8，先在当前 PowerShell 会话执行：

```powershell
..\scripts\use-local-jdk17.ps1
```

## 图片上传与 AI

- `/diet/photo/upload` 现在会先做真实图片读取；只有图片尺寸或体积超阈值时才压缩，然后再落盘并进入识别链路。
- 默认 `app.photo.ai.enabled=false`，因此会走可运行的本地 fallback，不会阻塞主链路开发。
- 固定提示词直接写在 `AdaptivePhotoAiRecognitionClient` 中，请按实际平台能力手动调整。
- 要接 duijieai 平台时，只需要改 `backend/src/main/resources/application-local.yml` 里的 `app.photo.ai.*`，再把 `AdaptivePhotoAiRecognitionClient` 里的请求体、鉴权头和响应解析改成平台真实契约即可。
