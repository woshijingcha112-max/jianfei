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
