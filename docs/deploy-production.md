# 部署上线清单

本文只记录当前后端 + 安卓 MVP 公网联调需要的最小上线步骤。真实密钥、数据库密码和服务器密码不写入仓库。

## 需要你提供的连接信息

- 服务器公网 IP 或域名
- SSH 端口
- 临时 `root` 密码或临时私钥
- 服务器系统版本
- MySQL 是否已经安装；如果已安装，需要提供库名、用户名、临时密码和连接地址
- 公网访问方式：直接开放 `8080`，还是走域名 + Nginx 反代到 `127.0.0.1:8080`
- 智谱 API Key：当前已按运行态需求直写在 `backend/src/main/resources/application.yml`，不再通过环境变量注入

连接完成后，你再修改服务器密码。

## 本地打包

后端：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/backend-mvn.ps1 -DskipTests package
```

产物：

```text
backend/target/diet-record-backend-0.1.0-SNAPSHOT.jar
```

安卓公网联调包：

```powershell
cd android
.\gradlew.bat :app:assembleDebug -PAPI_BASE_URL=http://服务器公网IP:8080/
```

如果后续改为域名和 HTTPS，则把 `API_BASE_URL` 改为：

```text
https://你的域名/
```

## 后端运行配置

后端配置通过环境变量注入，不改业务代码：

```bash
export SERVER_PORT=8080
export DB_URL='jdbc:mysql://127.0.0.1:3306/dev_cse_finance?characterEncoding=UTF-8&useUnicode=true&useSSL=false&tinyInt1isBit=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai'
export DB_USERNAME='root'
export DB_PASSWORD='替换为远端数据库密码'
export UPLOAD_ROOT='/opt/diet-app/uploads'
export UPLOAD_ACCESS_PREFIX='/uploads'
export LOG_DIR='/var/log/diet-app'
java -jar /opt/diet-app/diet-record-backend.jar
```

建议远端目录：

```text
/opt/diet-app/diet-record-backend.jar
/opt/diet-app/uploads/
/var/log/diet-app/
```

## 数据库

当前 schema 在：

```text
backend/src/main/resources/db/schema.sql
```

上线前需要在远端 MySQL 建库并导入该 schema。MySQL 端口 `3306` 默认不对公网开放；后端与 MySQL 同机时只允许本机访问即可。

## 防火墙

最小开放：

- `22` 或实际 SSH 端口：只允许你的管理 IP 更好
- `8080`：如果暂时直连后端公网联调
- `80/443`：如果使用 Nginx 和域名

默认不要向公网开放：

- `3306`：MySQL
- 后端上传目录的文件系统路径

## 上线后检查

远端后端启动后，先检查：

```bash
curl http://127.0.0.1:8080/diet/stat/today -X POST -H 'Content-Type: application/json' -d '{}'
```

如果直接公网开放 `8080`，再从本地检查：

```powershell
Invoke-WebRequest -Uri "http://服务器公网IP:8080/diet/stat/today" -Method POST -ContentType "application/json" -Body "{}"
```
