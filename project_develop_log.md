# Project Develop Log

## 2026-04-13 / 后端对象字段注释与 Lombok Data 收口
### 本次主要完成内容

- 已为后端对象层补充字段级中文注释，统一采用 `/** 注释 */` 形式，覆盖配置对象、PO、DTO、VO 以及图片识别内部结果对象。
- 已将普通 JavaBean 中非必要的手写 getter / setter 收口为 Lombok `@Data`，主要涉及 `AppProperties`、`ApiResponse`、`PhotoUploadVO`、`PhotoRecognitionItemVO` 等对象。
- 已保留 `record` 结构不改为普通类，仅为其组件补充中文注释，避免破坏现有不可变值对象语义。

### 本次验证结果

- `mvn -DskipTests compile`（系统 Maven + 项目 JDK 17）：通过

### 是否需要更新 spec

- 否

## 2026-04-13 / 识别结果页汇总卡与三按钮 UI 收口
### 本次主要完成内容

- 已删除识别结果页顶部重复的粉色结果头卡，页面首块改为单一“识别汇总”卡。
- 已将状态胶囊并入“识别汇总”右侧，统一收口为 `识别中 / n项 / 识别错误` 三种文案。
- 已放大“识别汇总”标题，并删除“保存前确认当前结果”及相关辅助说明文案，避免与下方内容重复。
- 已将“请求成功但返回空列表”的前端展示统一视为识别错误，固定提示文案为“这不是可以吃的东西喔~”，不再显示单独的“暂无结果”卡。
- 已将底部动作区从双按钮改为三按钮等宽布局：`确认保存 / 重拍 / 编辑`；其中“编辑”当前仅保留禁用态占位，不进入真实编辑流程。

### 本次验证结果

- `android\gradlew.bat :app:compileDebugKotlin`：通过

### 是否需要更新 spec

- 否

## 2026-04-13 / 喵呜桌面图标与演示 APK 交付
### 本次主要完成内容

- 将用户提供的猫爪 PNG 生成 Android launcher icon 资源，并接入 `AndroidManifest.xml` 的 `android:icon / android:roundIcon`。
- 将应用桌面显示名从“减肥记录”改为“喵呜”。
- 重新构建面向远端后端 `http://124.221.144.48:8080/` 的 Debug APK，并额外导出同目录演示包 `android/app/build/outputs/apk/debug/喵呜.apk`。
- 已将新 APK 安装并启动到真机 `192.168.5.7:35897`，用于用户演示。

### 本次验证结果

- `powershell -ExecutionPolicy Bypass -File scripts/backend-mvn.ps1 -DskipTests package`：通过
- `$env:JAVA_HOME=...; $env:ANDROID_HOME=...; $env:API_BASE_URL='http://124.221.144.48:8080/'; android\gradlew.bat :app:assembleDebug`：通过
- `adb -s 192.168.5.7:35897 install --no-streaming -r android/app/build/outputs/apk/debug/app-debug.apk`：成功
- `adb -s 192.168.5.7:35897 shell monkey -p com.dietrecord.app -c android.intent.category.LAUNCHER 1`：成功
- `aapt dump badging android/app/build/outputs/apk/debug/app-debug.apk`：确认 `application-label='喵呜'` 且 launcher icon 已写入各密度资源

### 是否需要更新 spec

- 否

## 2026-04-13 / 智谱视觉模型切换为 glm-4.6v
### 本次主要完成内容

- 后端智谱视觉模型从 `glm-5v-turbo` 切换为 `glm-4.6v`。
- 同步更新了运行配置、示例配置与 `AppProperties` 默认值，避免不同入口模型名不一致。
- 将 `Glm46VAdaptiveRecognitionDemoTest` 收口为单独的智谱 smoke test，不再依赖环境变量，也不再依赖百度校验链路。

### 本次验证结果

- `powershell -ExecutionPolicy Bypass -File scripts/backend-mvn.ps1 -Dtest=Glm46VAdaptiveRecognitionDemoTest test`：通过
- 实测日志显示智谱接口 `httpStatus=200`
- 实测日志显示 `model=glm-4.6v`
- 实测识别返回 `recognizedItemCount=8`

### 是否需要更新 spec

- 否

## 2026-04-13 / 识别结果页被相机预览遮挡修复
### 本次主要完成内容

- 修复二次点击拍照后结果页已切入但视觉上仍停留在拍摄界面的问题。
- 根因是 `PreviewView` 属于原生视图层，切入结果页时仍然覆盖在 Compose 结果页之上。
- 现在结果页打开时会立即隐藏相机预览视图，但识别异步流程继续执行；因此视觉上会立刻进入识别结果页并显示 loading。

### 本次验证结果

- `android\gradlew.bat :app:compileDebugKotlin`：通过

### 是否需要更新 spec

- 否

## 2026-04-13 / 相机残留请求号清理
### 本次主要完成内容

- 修复从首页再次进入拍照页时偶发直接拍照的问题。
- 进入拍照页、离开拍照流、重新拍照返回相机时，都会重置旧的拍照请求号，避免把上一轮请求误当成新请求消费。
- 相机页内部的“已消费请求号”改为普通内存态，不再跟随页面恢复持久化。

### 本次验证结果

- `android\gradlew.bat :app:compileDebugKotlin`：通过

### 是否需要更新 spec

- 否

## 2026-04-13 / 识别结果页即时切入与底部悬浮双按钮
### 本次主要完成内容

- 在拍照页内点击底部拍照按钮后，结果页立即覆盖显示，不再等待后端响应后才跳转。
- 结果页等待后端期间改为菜单级 loading；后端返回后直接替换为识别内容，失败则原位显示错误。
- 识别结果页展示时隐藏底部导航，避免页面仍然表现得像拍照页。
- 删除结果页无用的模拟失败按钮，改为底部悬浮双按钮：左侧浅绿色“确认保存”，右侧红色“重新拍照”。
- 相机页不再承担跳转结果页职责，只负责拍照；识别结果页切换由导航层控制，避免旧事件反复干扰。

### 本次验证结果

- `android\gradlew.bat :app:compileDebugKotlin`：通过

### 是否需要更新 spec

- 否

## 2026-04-13 / 拍照后立刻跳转识别结果页
### 本次主要完成内容

- 底部拍照按钮在拍照页内点击后，已改为立即进入识别结果页，不再等后端返回后才跳转。
- 识别结果页新增“识别中”状态，等待后端返回期间在明细区域展示菜单级 loading 占位。
- 新一轮拍照前会先清空上一轮识别结果，避免结果页瞬间闪出旧数据。
- 识别结果页容器改为独立背景，避免覆盖在相机预览上时露出底图。

### 本次验证结果

- `android\gradlew.bat :app:compileDebugKotlin`：通过
- `android\gradlew.bat :app:assembleDebug -PAPI_BASE_URL=http://124.221.144.48:8080/`：通过
- `scripts/deploy-android-debug.ps1 -SkipBuild -DeviceSerial 192.168.5.7:35897 -Launch`：安装成功并已启动到真机

### 是否需要更新 spec

- 否

## 2026-04-13 / 拍照入口改为纯二态切换
### 本次主要完成内容

- 已删除拍照按钮的假 loading。
- 当前页面不是拍照页时，点击底部拍照按钮只进入拍照页。
- 当前页面已经是拍照页时，点击底部拍照按钮才触发真实拍照与识别上传。
- 保持拍照页实时取景与顶层页面隔离逻辑不变。

### 本次验证结果

- `android\gradlew.bat :app:compileDebugKotlin`：通过

### 是否需要更新 spec

- 否

## 2026-04-13 / 首页-拍照-目标顶层隔离与拍照假 loading 缩短
### 本次主要完成内容

- 已将 `Home / Camera / Goal` 从共享导航栈切换改为顶层独立状态切换，避免首页、拍照页、目标页互相残留或串位。
- 识别结果页继续保留为拍照流上的单独覆盖层，不再作为顶层 tab 参与切换。
- 拍照入口的假 loading 从 1 秒缩短到 300ms；拍照页仍保持实时取景，识别等待期间不再锁死画面。
- 相机预览容器补了边界裁剪，减少 `PreviewView` 越界覆盖底栏点击区域的风险。

### 本次验证结果

- `android\gradlew.bat :app:compileDebugKotlin`：通过
- `android\gradlew.bat :app:assembleDebug -PAPI_BASE_URL=http://124.221.144.48:8080/`：通过
- `scripts/deploy-android-debug.ps1 -SkipBuild -DeviceSerial 192.168.5.7:35897 -Launch`：安装成功并已启动到真机

### 是否需要更新 spec

- 否

## 2026-04-13 / 拍照导航串位修复与取景持续显示
### 本次主要完成内容

- 已把底部导航从 `NavigationBarItem` 结构改为自绘三段式按钮，修复首页/拍照按钮点击串位问题。
- 已保留“第一次进入拍照只开预览，第二次点击圆形拍照按钮才真实拍照上传”的两段式交互。
- 已移除拍照页全屏 loading 遮罩，识别等待期间继续保留实时取景画面，只在底部圆形拍照按钮上展示 loading。

### 本次验证结果

- `android\gradlew.bat :app:compileDebugKotlin`：通过
- `android\gradlew.bat :app:assembleDebug -PAPI_BASE_URL=http://124.221.144.48:8080/`：通过
- `scripts/deploy-android-debug.ps1 -SkipBuild -DeviceSerial 192.168.5.7:35897 -Launch`：安装成功并启动成功

### 是否需要更新 spec

- 否

## 2026-04-13 / 拍照导航改为两段式触发与整屏预览
### 本次主要完成内容

- 底部导航中间“拍照”按钮已改为圆形视觉。
- 从其他导航进入拍照页时，当前只拉起相机预览，不会直接拍照上传；拍照按钮会先做 1 秒假 loading。
- 仅当用户已在拍照页再次点击底部拍照按钮时，才会真正触发拍照并上传识别。
- 拍照页已去掉页头、卡片边框和“实时取景”文案，拍照页主体改为整屏相机预览。

### 本次验证结果

- `android\gradlew.bat :app:compileDebugKotlin`：通过
- `android\gradlew.bat :app:assembleDebug -PAPI_BASE_URL=http://124.221.144.48:8080/`：通过
- `scripts/deploy-android-debug.ps1 -SkipBuild -DeviceSerial 192.168.5.7:35897 -Launch`：安装成功并启动成功

### 是否需要更新 spec

- 否

## 2026-04-13 / 远端智谱识别启用与配置收口
### 本次主要完成内容

- 已将后端智谱识别 `api-key` 从环境变量读取改为直写在 `application.yml`。
- 已把智谱默认识别 prompt 从 `ZhipuPhotoAiProvider` 中的默认常量下沉到 `application.yml`，避免运行关键配置散落在代码里。
- 已重新打包后端 jar，上传并替换远端 `/opt/diet-app/diet-record-backend.jar`，并重启 `diet-app` 服务。

### 本次验证结果

- `powershell -ExecutionPolicy Bypass -File scripts/backend-mvn.ps1 -DskipTests package`：通过
- 远端 `systemctl restart diet-app` 后状态为 `active`
- 使用真实测试图调用 `POST http://124.221.144.48:8080/diet/photo/upload`：成功
- 远端日志确认：
  - `ZhipuPhotoAiProvider` 返回 `httpStatus=200`
  - `AdaptivePhotoAiRecognitionClient` 日志为 `mainProvider=zhipu, effectiveProvider=zhipu`

### 是否需要更新 spec

- 否

## 2026-04-13 / 首页圆环尺寸微调
### 本次主要完成内容

- 按上一轮视觉调整幅度继续微调首页中间热量圆环尺寸，从 `92dp / 104dp` 上调为 `94dp / 106dp`。
- 仅调整首页圆环尺寸，不改数据流、后端接口、目标刷新逻辑或底部导航。

### 本次验证结果

- `android\gradlew.bat :app:compileDebugKotlin`：通过
- `android\gradlew.bat :app:assembleDebug -PAPI_BASE_URL=http://124.221.144.48:8080/`：通过
- `scripts/deploy-android-debug.ps1 -SkipBuild -DeviceSerial 192.168.5.7:35897 -Launch`：安装成功并启动成功

### 是否需要更新 spec

- 否

## 2026-04-13 / 首页圆环放大与左右指标居中
### 本次主要完成内容

- 已将首页热量圆环从上一版 `75dp / 84dp` 上调为 `92dp / 104dp`。
- 已将左右“饮食摄入 / 运动消耗”指标内部改为居中对齐，数值与标签同列居中展示。
- 已使用远程后端地址重新构建并安装启动到真机 `192.168.5.7:35897`。

### 本次验证结果

- `android\gradlew.bat :app:compileDebugKotlin`：通过
- `android\gradlew.bat :app:assembleDebug -PAPI_BASE_URL=http://124.221.144.48:8080/`：通过
- `scripts/deploy-android-debug.ps1 -SkipBuild -DeviceSerial 192.168.5.7:35897 -Launch`：安装成功并启动成功

### 是否需要更新 spec

- 否

## 2026-04-13 / 首页圆环与营养卡字号二次压缩
### 本次主要完成内容

- 顶部周视图和首页主卡字体继续下调一档。
- 热量圆环按当前尺寸约减半，并把粉色进度线进一步调细。
- 圆环中心剩余热量数字从上一版 `36sp` 调整到 `25sp`，同时保持大于左右指标数值字号。
- 左右“饮食摄入 / 运动消耗”字号下调并保留固定宽度，确保文案完整展示。
- 碳水、蛋白、脂肪三张卡删除“参考”字样，并下调标签与数字字号。
- 已使用远程后端地址重新构建并安装启动到真机 `192.168.5.7:35897`。

### 本次验证结果

- `android\gradlew.bat :app:compileDebugKotlin`：通过
- `android\gradlew.bat :app:assembleDebug -PAPI_BASE_URL=http://124.221.144.48:8080/`：通过
- `scripts/deploy-android-debug.ps1 -SkipBuild -DeviceSerial 192.168.5.7:35897 -Launch`：安装成功并启动成功

### 是否需要更新 spec

- 否

## 2026-04-13 / 首页周视图与热量圆环细化
### 本次主要完成内容

- 顶部周视图已改为只展示星期，不再展示日期数字。
- 首页主卡中的 `MM月dd日` 保留展示，并下调字号。
- 热量圆环区域按参考图结构继续收口：
  - 圆环尺寸下调，粉色进度线变细
  - “还可以吃(千卡)”下方数值字号下调
  - “饮食摄入”和“运动消耗”改为左右固定宽度布局，避免被中间圆环遮挡或挤压
- 碳水、蛋白、脂肪参考卡及以下内容未调整。
- 已使用远程后端地址重新构建并安装启动到真机 `192.168.5.7:35897`。

### 本次验证结果

- `android\gradlew.bat :app:compileDebugKotlin`：通过
- `android\gradlew.bat :app:assembleDebug -PAPI_BASE_URL=http://124.221.144.48:8080/`：通过
- `scripts/deploy-android-debug.ps1 -SkipBuild -DeviceSerial 192.168.5.7:35897 -Launch`：安装成功并启动成功

### 是否需要更新 spec

- 否

## 2026-04-13 / 首页顶部冗余元素删减与热量圆环文案收口
### 本次主要完成内容

- 已删除首页顶部“猫”入口、“喵喵日记”、“今天”胶囊和右侧占位按钮，仅保留周视图。
- 已将原顶部日期移入首页主卡标题位置，并按当前日期显示为 `MM月dd日` 格式。
- 已按参考图 2 收口热量圆环区域文案与位置：
  - 中心改为“还可以吃(千卡)”和剩余热量
  - 下方改为“推荐摄入 + 目标热量”
  - 左右补充“饮食摄入”和“运动消耗”
- 碳水、蛋白、脂肪参考卡以及其下方记录列表、底部导航未在本轮继续调整。
- 已使用远程后端地址重新构建并安装启动到真机 `192.168.5.7:35897`。

### 本次验证结果

- `android\gradlew.bat :app:compileDebugKotlin`：通过
- `android\gradlew.bat :app:assembleDebug -PAPI_BASE_URL=http://124.221.144.48:8080/`：通过
- `scripts/deploy-android-debug.ps1 -SkipBuild -DeviceSerial 192.168.5.7:35897 -Launch`：安装成功并启动成功

### 是否需要更新 spec

- 否

## 2026-04-13 / 目标保存后首页刷新修复与远程后端真机部署
### 本次主要完成内容

- 已修复“目标页修改热量目标后返回首页不刷新”的问题：
  - 首页 `refresh()` 不再只依赖饮食记录列表 flow 重新发射
  - 每次刷新会直接拉取今日记录和今日统计，并把最新 `targetCalories / consumedCalories` 写回首页状态
- 已使用远程后端地址重新构建 Debug APK：
  - `API_BASE_URL=http://124.221.144.48:8080/`
- 已安装并启动到真机 `192.168.5.7:35897`。

### 本次验证结果

- `android\gradlew.bat :app:compileDebugKotlin`：通过
- `android\gradlew.bat :app:assembleDebug -PAPI_BASE_URL=http://124.221.144.48:8080/`：通过
- `scripts/deploy-android-debug.ps1 -SkipBuild -DeviceSerial 192.168.5.7:35897 -Launch`：安装成功并启动成功

### 本次约束说明

- 本轮只修复首页刷新链路，不改后端接口、不改目标页表单语义、不扩展数据模型。
- 部署脚本仍会设置 `adb reverse tcp:8080`，但本次 APK 已通过 `API_BASE_URL` 构建参数固定指向远程后端。

### 是否需要更新 spec

- 否

## 2026-04-13 / 参照 React 导出稿做安卓首页最小 UI 替换
### 本次主要完成内容

- 已基于 `Mealflowreplica` 的 React 首页结构，按当前 Android 真实数据流重组首页视觉：
  - 顶部改为“猫咪日记 + 今天 + 周视图”的轻量头部
  - 首页主卡改为圆形热量进度卡，继续使用 `HomeUiState.summary.consumedCalories / targetCalories`
  - 新增碳水、蛋白、脂肪三张参考营养卡，当前明确作为静态参考视觉，不接入后端真实字段
  - 记录列表改为白底圆角餐食条目，继续使用现有 `DietRecordCardUiModel` 的摘要、时间、热量和标签
- 已同步调整底部导航视觉为粉白半透明顶圆角样式，并在首页选中态使用自绘猫爪视觉；导航目的地和路由逻辑保持不变。
- 本轮未引入 React 项目的 MUI、shadcn、lucide、motion 依赖，也未拷贝餐食静态图片到 Android 端。

### 本次约束说明

- 本轮只做首页和底部导航的最小视觉替换，不改后端接口、不改 Android 数据模型、不改拍照页、识别结果页和目标页。
- 三大营养素当前不是业务真实值，后续若要真实显示，需要单独扩展后端返回与 Android UI model。

### 本次验证结果

- `android\gradlew.bat :app:compileDebugKotlin`：通过
- `android\gradlew.bat :app:assembleDebug`：通过

### 是否需要更新 spec

- 否

## 2026-04-12 / 公网部署上线准备与远端后端启动
### 本次主要完成内容

- 后端继续采用环境变量承载部署配置，不把远端数据库密码、上传目录、日志目录和 AI Key 写入业务代码。
- 安卓端新增 `API_BASE_URL` 构建参数入口：
  - 可通过 `-PAPI_BASE_URL=http://124.221.144.48:8080/` 生成公网联调包
  - 未传参数时仍保留原本模拟器 `10.0.2.2:8080` 与真机 `127.0.0.1:8080` 的本地联调默认值
- 新增 `docs/deploy-production.md`，记录当前最小部署清单、环境变量、数据库、端口和 smoke test 命令。
- 已在远端 Ubuntu 22.04.5 服务器 `124.221.144.48` 安装 OpenJDK 17 与 MySQL 8。
- 已创建远端应用目录 `/opt/diet-app`、上传目录 `/opt/diet-app/uploads`、日志目录 `/var/log/diet-app` 和 systemd 服务 `diet-app.service`。
- 已导入当前 `schema.sql`，创建仅允许本机访问 MySQL 的应用用户，MySQL 仅监听 `127.0.0.1:3306`。
- 已部署本地后端 jar 到 `/opt/diet-app/diet-record-backend.jar` 并启动服务。
- 已通过 UFW 开放 `22/tcp` 与 `8080/tcp`，未开放 MySQL `3306`。
- 已用真实公网地址重新构建安卓 Debug APK，生成产物位于 `android/app/build/outputs/apk/debug/app-debug.apk`。

### 本次验证结果

- `powershell -ExecutionPolicy Bypass -File scripts/backend-mvn.ps1 -DskipTests package`：通过
- `android\gradlew.bat :app:assembleDebug -PAPI_BASE_URL=http://124.221.144.48:8080/`：通过
- 远端 `diet-app.service`：active running
- 本机公网访问 `POST http://124.221.144.48:8080/diet/stat/today` 携带日期参数：HTTP 200

### 本次暴露的缺陷或偏差

- 当前远端 `PHOTO_AI_ZHIPU_API_KEY` 暂未配置，后续拿到真实 Key 后需要更新 `/etc/diet-app.env` 并重启 `diet-app.service`。
- 当前只是公网直连 `8080` 的最小联调部署，尚未接入域名、HTTPS、Nginx 反代或正式发布签名 APK。
- 当前使用临时 root 密码完成部署；部署完成后应立即由用户修改 root 密码，并在后续切换到密钥登录。

### 是否需要更新 spec

- 否

## 2026-04-12 / 首页与拍照页结构重做（当前会话 Figma 工具阻塞）
### 本次约束说明

- 本轮目标仍按“首页结构重排 + 拍照页全屏化”推进，但当前会话未提供可直接写入 Figma 的工具入口，无法在本轮先完成 Figma 画稿。
- 因此本轮先落地安卓端代码改造，并把 Figma 稿补建保留为后续在工具可用会话中的同主题收口项，避免伪造未实际产出的设计资产。
- 本轮仍不改目标页、不改底部导航逻辑、不引入三大营养素假数据。

## 2026-04-12 / 首页总览卡二次收口与真机重新部署
### 本次主要完成内容

- 已根据真机截图继续收口首页总览卡的自适应阈值，修正第一轮方案在较宽手机竖屏下仍偏乐观的问题：
  - 首页外层横向 padding 的紧凑档位从 `<= 360dp` 放宽到 `<= 412dp`
  - 总览卡最外层粉色容器在手机竖屏下同步改用更小的外边距
  - 指标区不再只按总宽度 `< 320dp` 切单列，而是按“单个指标卡估算宽度”判断；当估算宽度不足或字体缩放偏大时，直接改为单列
- 已将单列指标块显式拉满可用宽度，避免堆叠后仍保留过窄卡片宽度
- 已将进度条区域的紧凑布局判断和指标区保持一致，避免总览卡上半区与下半区收口尺度不一致
- 已重新构建并重新安装最新 `Debug APK` 到无线调试设备 `192.168.5.7:35897`
- 已重新建立 `adb reverse tcp:8080 tcp:8080` 并重新启动应用

### 本次验证结果

- 已执行 `android\gradlew.bat :app:compileDebugKotlin`，通过
- 已执行 `android\gradlew.bat :app:assembleDebug`，通过
- 已执行 `adb install --no-streaming -r`，安装成功
- 已执行 `adb reverse tcp:8080 tcp:8080`，生效
- 已执行应用启动命令，成功拉起 `com.dietrecord.app`

### 本次暴露的缺陷或偏差

- 当前首页展示出的目标热量 `19612` 与剩余值 `17867` 来自后端真实返回，不是前端本轮排版拼接导致；若该数字本身不符合预期，需要单独排查目标保存或后端统计口径
- 无线调试场景下，配对端口与实际连接端口是两套信息；仅有六位数配对码不足以直接部署，仍需等 `_adb-tls-connect._tcp` 广播出真正连接端口

### 是否需要更新 spec

- 否

## 2026-04-12 / 首页与底部导航竖屏自适应收口
### 本次主要完成内容

- 已将首页竖屏自适应收口到当前任务直接相关的首页与底部导航，不扩散到拍照页、目标页正文或全局设计系统。
- 已调整首页外层与总览卡的横向间距策略：
  - `screenWidthDp <= 360` 时首页外层横向 padding 收窄到 `16.dp`
  - 总览卡内部内容区按紧凑宽度切换到更小的 `14.dp` 内边距和更紧的区块间距
- 已将“已摄入 / 目标”指标区改为按组件实际可用宽度和字体缩放切布局：
  - 指标容器宽度小于 `320.dp` 或 `fontScale >= 1.3f` 时自动切为单列堆叠
  - 其他竖屏手机宽度下保持双列
- 已修复指标卡内 `kcal` 被挤成竖排的问题：
  - 指标值与单位改为单个富文本 `Text`
  - 数字和单位用不同字阶渲染，但不再拆成两个横向 `Text` 抢宽度
- 已同步收口总览卡内的标题和进度信息展示：
  - 紧凑宽度下“今日总览”和指标值采用更小的局部字阶
  - 进度条说明文本改为双侧分栏承载，避免窄屏下相互挤压
  - 目标 badge 的偏移计算按更保守的宽度估算收口，避免贴边
- 已将底部导航从自定义占位方块切回标准 Material3 导航：
  - 使用 `NavigationBar + NavigationBarItem`
  - 新增官方 Material icons extended 依赖
  - 首页、拍照、目标分别接入 `Home / CameraAlt / Flag` 图标

### 本次验证结果

- 已执行 `android\gradlew.bat :app:compileDebugKotlin`，通过
- 已执行 `android\gradlew.bat :app:assembleDebug`，通过

### 本次暴露的缺陷或偏差

- 当前仓库仍没有 Compose Preview、截图测试或 UI 自动化基线，本轮只能先用编译与打包通过作为机械验证，真机/模拟器的 `360dp / 393dp / 412dp + 字体缩放` 手工回归仍需后续补做
- 当前构建依赖项目内 `tools/android-sdk`，在 Codex 沙箱内直接跑 Gradle 会因为 SDK 路径和本机 Gradle native library 加载受限而失败，需要在放宽权限后执行验证

### 是否需要更新 spec

- 否

## 2026-04-12 / 首页、拍照、目标页去过程化文案与底部导航收口
### 本次主要完成内容

- 收窄三个核心页的顶部标题区：
  - `AppPageHeader` 改为支持无副标题，减少顶部内边距与装饰高度
  - 首页、拍照页、目标页均移除过程性副文案与页头角标
- 收窄底部三栏导航：
  - 去掉 tab 内部的“首 / 拍 / 目”文字方块
  - 改为空白占位方框，保留底部文字标签，便于后续替换成 icon
  - 同时整体降低导航高度与圆角厚重感
- 清理三个页面中的实现过程文案：
  - 首页移除“首页总览和记录列表……”“把关键数字收进一张……”以及 MySQL/联调类描述
  - 拍照页移除 `/diet/photo/upload`、链路说明、失败模拟入口等调试/实现说明
  - 目标页移除“保存后同步首页”“本轮范围”等说明与失败模拟入口
- 简化主要操作按钮：
  - 首页主按钮收口为“拍照记录”
  - 目标页主按钮收口为“保存”
  - 三页按钮不再展示 supporting text 与角标文字

### 本次验证结果

- 已执行 `android\gradlew.bat :app:compileDebugKotlin`，通过
- 已执行 `android\gradlew.bat :app:assembleDebug`，通过

### 是否需要更新 spec

- 否

## 2026-04-12 / 导航到达刷新机制收口
### 本次主要完成内容

- 已将首页与目标页原先依赖 `onResume` 的刷新方式收口为更稳定的通用机制：
  - 新增应用级 `AppRefreshCoordinator`，只记录“成功修改版本号”
  - 新增 `AppDestinationRefreshEffect`，目标页每次导航到达时刷新一次
  - 若页面停留期间又收到新的成功修改版本，则当前页再刷新一次
- 已将成功修改链路接入统一刷新版本：
  - 目标保存成功后递增全局修改版本
  - 识别结果保存成功后递增全局修改版本
- 已将首页与目标页接到统一到达刷新机制，移除对 `AppOnResumeEffect` 的依赖
- 目标页在本页保存成功后会保留成功提示，并立即刷新当前页数据；之后跳转到首页时，首页也会再次刷新

### 本次约束说明

- 本轮没有引入复杂导航结果传参或跨页业务对象同步，只保留轻量的全局修改版本号，避免过度设计
- 识别结果页保存成功后仍按现有链路返回首页，不在结果页停留额外做复杂状态恢复

### 是否需要更新 spec

- 否

## 2026-04-12 / 识别结果保存卡住排查与日志阻塞修复
### 本次主要完成内容

- 已根据 `2026-04-12 05:14:27` 真机日志确认：安卓端实际发出了 `POST /diet/record/save`，问题不是“前端未调用后端”。
- 已通过 8080 端口 Java 进程线程栈定位根因：
  - 当前监听 8080 的进程为 `com.dietrecord.backend.DietRecordApplication`
  - `http-nio-8080-exec-*` 请求线程进入 `DietRecordServiceImpl.save()` 后，阻塞在首条 `log.info(...)`
  - 具体卡点位于 Logback `ConsoleAppender` 向控制台 `FileOutputStream.writeBytes(...)` 写日志
- 已调整后端日志配置：
  - 新增滚动文件日志 `../tools/logs/backend-app.log`
  - 控制台输出切到 `AsyncAppender`
  - 文件输出保留同步落盘
  - 控制台开启 `neverBlock=true`，避免控制台输出阻塞直接拖死接口线程

### 本次暴露的缺陷或偏差

- 当前后端核心请求链路仍把入口日志放在同步业务线程中，只要底层控制台输出阻塞，就会把接口吞住，表现为“客户端一直保存中、日志文件也看不到接口调用”。
- 之前查看的 `tools/logs/backend-spring-boot-run.log` 不是运行中 8080 进程的可靠唯一日志出口，不能单靠它判断请求是否进入后端。

### 是否需要更新 spec

- 否

## 2026-04-12 / 真机拍照识别超时排查与目标页当前体重可编辑修复

### 本次主要完成内容

- 已将智谱主识别调用从流式聚合改为非流式 `chat/completions`，避免后端持续收流但接口迟迟不收口。
- 已补充拍照识别链路的前后端独立日志：
  - 安卓端新增 `PhotoUpload` 上传日志，记录开始、接口返回、异常类型与耗时。
  - `CameraViewModel` 新增拍照识别总耗时日志。
  - 后端 `PhotoController / PhotoService / PhotoRecognitionService / ZhipuPhotoAiProvider` 新增接口总耗时、结构化菜名、识别条目数等摘要日志。
- 已修复目标页“当前体重无法编辑”问题，并打通到后端保存：
  - 前端目标页输入框取消只读。
  - 安卓 `GoalSaveDTO` 新增 `currentWeight`。
  - 后端 `GoalSaveDTO / GoalServiceImpl` 同步支持保存当前体重到 `user_profile.weight_kg`。
- 已重新执行后端 `compile` 与安卓 `:app:assembleDebug`，均通过。

### 本次暴露的缺陷或偏差

- 真机调试包安装仍依赖本地 `adb` 访问宿主目录；当前 Codex 沙箱内直接执行安装命令会因为 `C:\\Users\\CodexSandboxOffline\\.android` 无权限而失败，需要在放宽权限后再执行一次真机安装。
- 后端代码已经变更，但如果本机当前仍跑着旧进程，则必须重启后端后真机联调才会生效。

### 是否需要更新 spec

- 否

## 2026-04-12 / 真机 Debug 部署脚本补充

### 本次主要完成内容

- 新增 `scripts/deploy-android-debug.ps1`，用于统一执行真机 Debug 部署。
- 脚本默认执行：
  - 构建安卓 Debug APK。
  - 自动选择无线调试 TCP 设备。
  - 安装 `android/app/build/outputs/apk/debug/app-debug.apk`。
  - 自动建立 `adb reverse tcp:8080 tcp:8080`。
- 已使用 `-SkipBuild` 验证脚本，安装成功并确认 reverse 列表包含 `host-25 tcp:8080 tcp:8080`。

### 使用方式

- 常规部署：`powershell -ExecutionPolicy Bypass -File scripts/deploy-android-debug.ps1`
- 已经打包后只安装并建立 reverse：`powershell -ExecutionPolicy Bypass -File scripts/deploy-android-debug.ps1 -SkipBuild`
- 指定设备：`powershell -ExecutionPolicy Bypass -File scripts/deploy-android-debug.ps1 -DeviceSerial 192.168.5.7:40277`

### 是否需要更新 spec

- 否

## 2026-04-12 / 目标保存失败链路收口

### 本次主要完成内容

- 已根据真机日志定位目标保存失败根因：
  - 安卓端 `/goal/save` 收到 HTTP 200。
  - 业务响应实际是后端 `CannotGetJdbcConnectionException`。
  - 后端默认数据库地址仍指向 `192.168.72.59:3306`，而当前本机可用 MySQL 是 `127.0.0.1:3306`。
- 已将后端默认开发数据库配置切回本机 MySQL：
  - `DB_URL` 默认值改为 `jdbc:mysql://127.0.0.1:3306/dev_cse_finance`
  - `DB_USERNAME` 默认值改为 `root`
  - `application-local.example.yml` 同步改成本机示例。
- 已调整安卓目标保存仓储：
  - `/goal/save` 成功后直接更新本地 `goalFlow`。
  - 不再在保存成功后强制追加一次 `refreshGoal()`，避免“后端已保存，但后续刷新失败”误报成保存失败。
  - 补充保存接口业务响应 `code/msg` 日志。
- 已重新执行后端 `compile`、安卓 `:app:assembleDebug`，并通过部署脚本安装到真机且建立 `adb reverse tcp:8080 tcp:8080`。

### 本次验证结果

- 本机 MySQL 已确认可用：`user_profile id=1` 可查询到 `weight_kg=70.0 / target_weight=65.0 / daily_cal_limit=1500`。
- 最新 Debug APK 已安装到真机。

### 是否需要更新 spec

- 否

## 2026-04-12 / 部署脚本补充后端启动命令

### 本次主要完成内容

- 已在 `scripts/deploy-android-debug.ps1` 中补充后端启动命令输出：
  - `powershell -ExecutionPolicy Bypass -File "D:\source\code\jianfei\scripts\backend-mvn.ps1" spring-boot:run`
- 已新增 `-StartBackend` 参数：
  - 默认不启动后端，只输出命令并继续执行安卓部署。
  - 需要一键启动后端时，可执行 `powershell -ExecutionPolicy Bypass -File scripts/deploy-android-debug.ps1 -StartBackend`。
- 已做 PowerShell 脚本语法检查，通过。

### 是否需要更新 spec

- 否

## 2026-04-12 / 页面返回刷新机制通用化

### 本次主要完成内容

- 已定位“目标页返回首页后内容不刷新”的直接原因：
  - `HomeViewModel` 之前只在 `init` 中拉取一次数据。
  - 导航返回后复用旧 ViewModel，不会自动重新请求首页数据。
- 已新增通用组件 `AppOnResumeEffect`：
  - 位置：`android/app/src/main/java/com/dietrecord/app/core/ui/components/AppLifecycleEffects.kt`
  - 作用：页面每次恢复到前台时统一触发回调。
- 已将该通用机制接入：
  - 首页：恢复时调用 `HomeViewModel.refresh()`
  - 目标页：恢复时调用 `GoalViewModel.refresh()`
- 已将首页与目标页的首刷逻辑从 `init` 转为公开 `refresh()`，避免后续新增页面时继续沿用“一次性初始化、不支持返回刷新”的模式。
- 已执行安卓 `:app:assembleDebug`，通过。

### 本次约束说明

- 本轮只改通用刷新机制与页面数据重拉入口。
- 未部署 APK 到真机。

### 是否需要更新 spec

- 否

## 2026-04-12 / 真机网络基址切换

### 本次主要完成内容

- 已将安卓网络默认基址改为按运行环境区分：
  - 模拟器继续使用 `10.0.2.2:8080`
  - 真机改为通过 `adb reverse` 访问 `127.0.0.1:8080`
- 这样可以避免真机继续访问模拟器专用地址，也绕开局域网入站和 Windows 防火墙问题。
- 已将安卓端 `OkHttp` 超时策略调大：
  - `connectTimeout = 20s`
  - `readTimeout = 90s`
  - `writeTimeout = 90s`
  - `callTimeout = 90s`
- 这样可以避免后端已进入 AI 识别但前端先因等待时间过短而主动超时。

### 本次暴露的缺陷或偏差

- 真机拍照失败并不是 CameraX 本身异常，而是上传识别阶段访问了错误地址。
- 同时，本轮真机测试时本机后端未启动，也会导致上传链路失败。

### 是否需要更新 spec

- 否

## 2026-04-12 / 拍照识别链路按 MVP 收口

### 本次主要完成内容

- 已将后端拍照识别运行链路收口为：`GLM-5V-Turbo` 主识别 + 本地 fallback。
- 已保留百度菜品识别 provider 代码与配置结构，但默认关闭 `app.photo.ai.baidu.enabled`，当前不再发起实际百度调用。
- 已在识别编排处补充中文注释，记录后续可演进方向：
  - 百度主结果前置，优先给前端返回菜品级结果。
  - 多模态继续负责食材级明细、重量和热量估算。
- 本次未调整接口返回结构；`structuredResult` 中的校验信息在当前配置下会体现为“未执行百度校验”。

### 本次未完成内容

- 未把“百度主结果前置 + 多模态补明细”做成真实双阶段调用。
- 未改安卓端去消费 `structuredResult`。

### 本次暴露的缺陷或偏差

- 之前文档把当前运行态写成了“智谱 + 百度校验 + 本地 fallback”三层并行执行，但这不再符合当前 MVP 收口后的真实状态。
- 识别结果页当前仍主要依赖 `recognizedItems`，即便后端已返回 `structuredResult`，前端也不会直接展示食材级结构化信息。

### 这些缺陷 / 未完成项将在第几步收口

- 真机测试若确认当前单模型链路可接受，下一步优先完成安卓端端到端回归。
- 若后续要优化首屏反馈，再把百度前置方案落成真实二段式或并行方案，而不是继续停留在注释层。

### 是否需要更新 spec

- 否

### 备注

- 本次调整是 MVP 收口，不是能力回退；百度接入代码仍保留，后续可按真实需求重新启用。

## 2026-04-12 / GLM-4.6V 主识别接入与百度校验保留

### 本次主要完成内容

- 已将图片识别配置从单一百度参数重构为：
  - 全局 `app.photo.ai.enabled / provider / timeout-millis / default-meal-type`
  - `app.photo.ai.zhipu.*`
  - `app.photo.ai.baidu.*`
- 已按智谱官方 `GLM-4.6V` 文档完成后端接入：
  - 调用入口为 `POST https://open.bigmodel.cn/api/paas/v4/chat/completions`
  - 图片以 Base64 方式放入 `messages[].content[].image_url.url`
  - 关闭 `thinking` 并下调 `max_tokens`，避免视觉结构化场景超时
- 已把原有单类 `AdaptivePhotoAiRecognitionClient` 收敛为少量适配点结构：
  - 新增 `PhotoAiProvider` 接口
  - 新增 `ZhipuPhotoAiProvider`
  - 新增 `BaiduDishPhotoAiProvider`
  - `AdaptivePhotoAiRecognitionClient` 只负责主 provider 选择、百度校验、fallback 与统一结构化结果组装
- 已新增统一结构化结果模型，并回写到上传接口：
  - `/diet/photo/upload` 在原有 `photoUrl + recognizedItems` 基础上，新增 `structuredResult`
  - 结构化字段已按本轮约定输出 `图片识别结果 / 整道菜信息 / 食材明细 / 汇总信息 / 校验信息`
- 已修复一处百度历史兼容问题：
  - 百度 `probability / calorie` 实际经常返回字符串，本轮已补数值字符串解析，不再丢失热量与置信度
- 已新增 `Glm46VAdaptiveRecognitionDemoTest`
  - 不进入 Spring 容器
  - 不依赖数据库
  - 直接用 `tools/picture` 下真实餐食图联调 `GLM-4.6V + 百度校验 + 结构化组装`

### 本次验证结果

- 已执行 `powershell -ExecutionPolicy Bypass -File scripts/backend-mvn.ps1 -DskipTests test-compile`
  - 通过
- 已执行 `powershell -ExecutionPolicy Bypass -File scripts/backend-mvn.ps1 -Dtest=Glm46VAdaptiveRecognitionDemoTest test`
  - 通过
- 真实图片联调结果：
  - `GLM-4.6V` 主识别已成功返回 `麻辣烫`
  - 返回的食材明细包含 `青菜 / 藕片 / 午餐肉 / 金针菇 / 豆腐 / 丸类 / 木耳 / 腐竹 / 培根`
  - 百度校验仍返回 `麻辣烫`，置信度 `0.994832`
  - 当前结构化汇总热量为 `520`

### 本次未完成内容

- 安卓端尚未消费 `structuredResult` 做食材级展示
- 结构化结果中的部分 `识别置信度` 仍可能为空，当前保持尊重模型原始输出，不做强行伪造
- 仍未完成安卓模拟器或真机端到端手工联调

### 本次暴露的缺陷或偏差

- `GLM-4.6V` 在默认思考模式下超时明显，当前通过关闭 `thinking` 和下调 `max_tokens` 才稳定落入可接受时延
- 当前 `recognizedItems` 仍是前端主消费字段，`structuredResult` 只是后端已返回，前端尚未接入显示

### 这些缺陷 / 未完成项将在第几步收口

- 下一步若要把食材级信息展示到前端，需要安卓端补 `structuredResult` 的网络模型与页面展示
- 随后做一次安卓端真实拍照链路手工联调，确认拍照上传、结构化结果展示、保存记录三段一致

### 是否需要更新 spec

- 否

### 备注

- 本轮保持了“少量适配点”原则，没有为了未来平台扩展提前铺设大面积架构

## 2026-04-12 / 主识别模型切换为 GLM-5V-Turbo

### 本次主要完成内容

- 已将智谱主识别模型从 `GLM-4.6V` 切换为 `GLM-5V-Turbo`
- 调用方式保持不变，继续沿用 `chat/completions + image_url + text` 的图片理解模式
- 继续保留当前已验证可用的低延迟参数：
  - `thinking.type = disabled`
  - `max_tokens = 2048`
  - `temperature = 0.1`
  - `timeout-millis = 45000`

### 本次验证结果

- 已重新执行 `powershell -ExecutionPolicy Bypass -File scripts/backend-mvn.ps1 -Dtest=Glm46VAdaptiveRecognitionDemoTest test`
  - 通过
- 同一张测试图下，`GLM-5V-Turbo` 主识别耗时约 `23.98s`
- 结构化结果质量较 `GLM-4.6V` 更细：
  - 识别出 `藕片 / 午餐肉或火腿片 / 培根或五花肉片 / 蟹棒 / 鱼丸或虾丸 / 牛肉丸 / 金针菇 / 青菜 / 木耳 / 腐竹或豆腐皮 / 油豆腐或豆腐泡`
  - 同时保留百度 `麻辣烫` 校验结果

### 本次未完成内容

- 尚未把 `GLM-5V-Turbo` 与 `GLM-4.6V` 做更大样本量对比
- 安卓端仍未展示 `structuredResult`

### 本次暴露的缺陷或偏差

- `GLM-5V-Turbo` 对复杂食材的识别粒度更细，但热量估算明显更激进，当前总估算热量提升到 `1059`
- 当前仍需要后续通过提示词或前端人工确认机制收口高热量偏保守问题

### 这些缺陷 / 未完成项将在第几步收口

- 下一步如继续保留 `GLM-5V-Turbo`，应补 3 到 5 张真实餐食图做横向样本验证
- 若模型粒度满意，再进入安卓结构化结果展示和人工确认交互

### 是否需要更新 spec

- 否

### 备注

- 本轮只切换主识别模型，不改变现有适配层结构

## 2026-04-12 / 图片识别外调日志补强

### 本次主要完成内容

- 已为智谱主识别增加详细外调日志：
  - 请求摘要
  - 原始响应摘要
  - 单次请求耗时
- 已为百度校验增加详细外调日志：
  - `access_token` 获取请求与响应摘要
  - 菜品识别请求摘要
  - 菜品识别响应摘要
  - token 获取耗时、菜品识别耗时、总耗时
- 已为识别编排层增加汇总日志：
  - 整体请求编号
  - 主 provider / 生效 provider
  - 候选数
  - 结构化菜名
  - 总耗时

### 本次约束说明

- 日志已按“详细但不失控”处理：
  - 不记录 `API Key / Secret Key`
  - 不把整张图片的 Base64 原文打入日志
  - 仅记录图片大小、尺寸、请求参数摘要和响应摘要

### 是否需要更新 spec

- 否

## 2026-04-12 / 智谱切换流式返回验证

### 本次主要完成内容

- 已将智谱主识别调用改为 `stream = true`
- 已补充流式分片日志，记录每个分片的简要内容
- 已将智谱耗时日志拆分为：
  - `firstByteElapsedMs`
  - `streamReadElapsedMs`
  - `totalElapsedMs`
- 已将本次流式联调输出完整落盘到测试日志文件，便于后续人工排查

### 本次验证结果

- 已使用同一张 `tools/picture` 测试图重跑 `GLM-5V-Turbo + 百度校验`
- 当前流式日志显示：
  - 智谱首包约 `1.7s`
  - 智谱流式正文读取约 `8~9s`
  - 智谱总耗时约 `10.7s`
  - 百度校验总耗时约 `12.0s`
  - 编排总耗时约 `22.8s`

### 本次暴露的缺陷或偏差

- 智谱流式输出的分片非常碎，当前一次请求可拆成数百个 chunk，日志量明显变大
- 如果后续长期保留逐 chunk `INFO` 日志，生产环境日志量会偏大，后续可能需要按环境降级到 `DEBUG`

### 是否需要更新 spec

- 否

## 2026-04-12 / spec 与 AGENTS 文档职责重构

### 本次主要完成内容

- 已重构 `spec.md` 的职责定位：
  - `spec.md` 只保留项目硬边界与稳定软规范
  - 不再把项目阶段、完成情况、当前重点、当前未完成等进度信息写入 `spec.md`
- 已重写 `spec.md` 的软规范内容方向：
  - 软规范改为记录稳定的工程约束、抽象复用原则、注释日志要求、AI 接入约束
  - 不再把软规范当作项目进度或当前轮次策略的载体
- 已微调 `AGENTS.md` 中与 `spec` 相关的执行口径：
  - 开工顺序改为先读 `spec.md` 的硬边界和软规范，再读 `feature_status.md`
  - 阶段性策略、试验性方案、临时折中统一写入 `project_develop_log.md`
  - 只有当规则已沉淀为稳定约束时，才允许更新 `spec.md`
- 已继续收紧 `spec.md` 的表达：
  - 合并重叠的硬边界条目，减少针对“如何写 spec”本身的冗余约束
  - 将 `PO / DTO / VO / BO` 约束从硬边界移回软规范
  - 弱化多模型接入条目，明确目标是“减少散落、避免过度设计”，而不是强推大而全抽象

### 本次未完成内容

- 未同步清理历史开发日志中早期“soft spec 承载进度”的历史痕迹，本轮只修正文档职责和后续执行口径

### 本次暴露的缺陷或偏差

- 旧版 `spec.md` 的“当前软规范”混入了较多阶段性推进内容，容易把规范文档误用为迭代说明
- 旧版 `AGENTS.md` 中“通过当前迭代需求重构软规范”的描述，容易诱导在每轮开发时把进度性内容写回 `spec.md`

### 这些缺陷 / 未完成项将在第几步收口

- 后续若继续调整工程规范，只在规则真正稳定后更新 `spec.md`
- 若只是本轮策略或试验路线变化，统一写入 `project_develop_log.md`

### 是否需要更新 spec

- 是

### 备注

- 本轮是文档治理调整，不涉及业务逻辑、接口契约和项目完成度真值变更

## 2026-04-11 / 百度菜品识别生产接入落地

### 本次主要完成内容

- 已将后端 `app.photo.ai` 配置切换到百度菜品识别：
  - `provider = baidu`
  - `token-endpoint = https://aip.baidubce.com/oauth/2.0/token`
  - `endpoint = https://aip.baidubce.com/rest/2.0/image-classify/v2/dish`
  - 已写入本轮提供的 `appId / API Key / Secret Key`
- 已重写 `AdaptivePhotoAiRecognitionClient` 的真实外调逻辑：
  - 先获取并缓存 `access_token`
  - 再调用百度菜品识别接口
  - 将返回的 `name / probability / calorie` 映射到现有候选模型
  - 调用异常时继续保留本地 fallback，避免主链路直接中断
- 已补齐百度图片规格兼容：
  - `AppProperties.Image` 新增 `minDimension = 15`
  - `PhotoImageProcessingService` 会在最短边小于 `15px` 时先放大到平台可接受范围
  - `BaiduDishRecognitionDemoTest` 也同步补了最短边兼容，避免 demo 被平台参数校验直接拒绝
- 已完成验证：
  - `powershell -ExecutionPolicy Bypass -File scripts/backend-mvn.ps1 -DskipTests compile` 通过
  - 使用仓库内 `android/app/src/main/assets/sample_meal.png` 和本轮提供的 AK/SK 执行 `BaiduDishRecognitionDemoTest` 成功
  - 百度接口实际返回 `name = 非菜`，说明鉴权和接口已打通，但当前样例图不是有效餐食图
- 已补一处前后端契约兜底：
  - 当百度结果未命中当前 `food_library` 时，后端默认返回 `tagColor = 2`
  - 避免安卓端 `recognizedItems.tagColor` 非空约束被 `null` 打穿

### 本次未完成内容

- 未拿真实餐食图片验证百度识别业务效果
- 未完成安卓端真机或模拟器整链路手工联调
- 未移除 fallback 策略，本轮仍保留兜底能力

### 本次暴露的缺陷或偏差

- 仓库内 `sample_meal.png` 实际是 1x1 占位图，只适合验证接口连通，不适合验证识别准确率
- 当前生产配置已写入明文 AK/SK，符合本轮要求，但后续若要上线必须改回安全配置

### 这些缺陷 / 未完成项将在第几步收口

- 下一步由你提供真实餐食图片，先跑一次百度 demo，再走安卓拍照上传链路做端到端联调
- 若业务效果稳定，再视需要决定是否移除 fallback 或把明文凭证迁回环境变量

### 是否需要更新 spec

- 否

### 备注

- 本轮已经完成百度生产接入，不再依赖旧平台 `duijieai`

## 2026-04-11 / 百度菜品识别接入准备确认与 Demo 单测落地

### 本次主要完成内容

- 已确认百度菜品识别接入前置条件：需要百度智能云账号、已创建应用的 `API Key / Secret Key`，正式调用前需先换取 `access_token`
- 已基于百度官方文档确认关键接口与约束：
  - `POST https://aip.baidubce.com/oauth/2.0/token` 用于获取 `access_token`
  - `POST https://aip.baidubce.com/rest/2.0/image-classify/v2/dish` 用于菜品识别
  - 图片需以 `application/x-www-form-urlencoded` 方式提交，核心参数为 `image`
- 已新增 `backend/src/test/java/com/dietrecord/backend/BaiduDishRecognitionDemoTest.java`
  - 只读取环境变量与本地图片路径
  - 不进入 Spring 容器
  - 不修改任何生产逻辑
  - 支持直接提供 `BAIDU_AIP_ACCESS_TOKEN`，也支持通过 `BAIDU_AIP_API_KEY / BAIDU_AIP_SECRET_KEY` 现场换取 token
- 已完成最小验证：`powershell -ExecutionPolicy Bypass -File scripts/backend-mvn.ps1 -Dtest=BaiduDishRecognitionDemoTest test` 通过，当前因未提供凭证与图片，测试以手动跳过结束

### 本次未完成内容

- 未把生产主链路里的旧平台适配切换到百度
- 未完成携带真实百度凭证和示例图片的在线识别实调
- 未改动安卓端拍照上传后的生产识别落点

### 本次暴露的缺陷或偏差

- 当前仓库生产代码仍保留旧平台适配与 fallback 逻辑，百度只完成了 Demo 级联调入口
- 若后续直接改生产适配，必须先处理好百度 `access_token` 缓存与刷新，而不是每次请求都现场换 token

### 这些缺陷 / 未完成项将在第几步收口

- 下一步先由用户提供百度应用凭证与测试图片，跑通 `BaiduDishRecognitionDemoTest`
- 确认百度返回契约后，再进入后端生产适配切换与安卓端整链路联调

### 是否需要更新 spec

- 否

### 备注

- 本轮新增的是独立测试 Demo，不是生产能力切换

## 2026-04-11 / 安卓端中文注释与结构日志基线补齐

### 本次主要完成内容

- 已为 Android 应用入口、导航容器、Repository、ViewModel 补充中文结构注释
- 已在 Android 状态边界层补充关键日志，主要覆盖应用启动、数据加载、拍照识别、目标保存、识别结果保存等链路
- 已把导航与状态层命名、文案向中文可读性收口，便于 Java 开发快速理解模块职责
- 已完成 Android `:app:assembleDebug` 构建验证，确认本轮未引入编译错误

### 本次未完成内容

- 未对全部 Compose 页面做深度文案统一，本轮优先覆盖结构层与关键链路
- 未增加新的日志框架或通用工具类，保持现有 Android 技术栈不变

### 本次暴露的缺陷或偏差

- 部分历史页面文案仍有进一步统一空间，后续若继续收口，需要继续按页面层补齐

### 这些缺陷 / 未完成项将在第几步收口

- 下一步可继续补 Home、Camera、Goal、Recognize 四个页面的中文文案和注释一致性

### 是否需要更新 spec

- 否

### 备注

- 本轮不改文件目录、不改业务逻辑，只补注释、日志和可读性

## 2026-04-11 / 后端中文注释、日志规范与工具依赖基线补齐

### 本次主要完成内容

- 已为后端全部 controller 补充中文类注释与接口方法注释
- 已为后端 service 接口补充中文类注释与方法注释
- 已为复杂 service 逻辑补充中文单行注释，并按逻辑块换行分隔
- 已新增 `backend/src/main/resources/logback-spring.xml`，统一控制台日志格式
- 已把后端日志写法统一切换为 Lombok `@Slf4j`
- 已在 `backend/pom.xml` 引入 `hutool-all` 依赖，占位但暂不使用
- 已更新 `spec.md` 硬边界，强制优先复用工具类、基础封装与通用 UI 组件

### 本次未完成内容

- 未对全部历史代码补齐更深层领域注释，本轮只覆盖 controller、service 与日志基线
- 未新增 hutool 工具封装，本轮只完成 Maven 依赖占位
- 未执行安卓端验证，本轮验证范围只覆盖后端

### 本次暴露的缺陷或偏差

- 部分历史文档和旧代码仍存在英文信息或历史占位文案，后续若继续收口，需要按模块继续统一

### 这些缺陷 / 未完成项将在第几步收口

- 下一步可继续下沉到 mapper、DTO/VO 与公共配置层的注释统一
- 若后续新增前后端共性能力，必须先评估是否可以复用现有工具类与通用组件

### 是否需要更新 spec

- 是

### 备注

- 本轮属于工程规范补齐，不改变现有业务边界

## 当前摘要

- 当前阶段：核心功能真实化进行中，拍照识别主链路已切到真实拍照上传与 AI 适配骨架，待安卓端手工联调与 AI 平台实连
- 最近一次更新时间：2026-04-11
- 当前总体状态：前后端基础骨架、本地开发环境、最小 harness 控制层与首页主链路真实接口已落地；拍照页已接入 CameraX 预览与真实拍照上传，后端已接入图片按需压缩与 AI 适配骨架，默认 fallback 可跑通主链路
- 当前最关键未收口项 1：安卓端未完成模拟器/真机手工回归，且本机 Gradle native service 环境异常导致构建验证未完成
- 当前最关键未收口项 2：duijieai 平台真实契约仍未实连验证，当前仍依赖后端 fallback
- 当前最关键未收口项 3：搜索、删除记录、体重记录与人工修正增强交互仍未纳入本轮

## 开发日志

### 2026-04-11 / 拍照真实链路与 AI 适配骨架落地

#### 本次主要完成内容

- 已完成安卓端拍照主链路真实化：
  - 接入 CameraX 依赖与 `CAMERA` 权限
  - 拍照页从示例餐图入口切换为实时预览、拍照、生成临时图片文件并上传
  - `RecognitionRepository` 改为接收真实拍照文件上传，不再读取 `sample_meal.png`
  - 识别结果页与保存链路继续复用现有 session 状态与接口契约
- 已完成后端图片处理与 AI 适配骨架：
  - `/diet/photo/upload` 改为先做图片读取与按需压缩，再保存处理后图片
  - 新增 `PhotoImageProcessingService` 处理“非必要不压缩”的图片规则
  - 新增 `PhotoAiRecognitionClient` 与 `AdaptivePhotoAiRecognitionClient`
  - 固定提示词已直接写入 AI 调用参数构造
  - duijieai 请求体、鉴权头与响应解析已集中放在 `AdaptivePhotoAiRecognitionClient`，方便后续手动改平台契约
  - `PhotoRecognitionService` 已从文件名 mock 打分切到“AI 候选项 -> 菜单库匹配 -> VO 返回”
- 已同步更新 `spec.md` 当前软规范，允许真实拍照、按需压缩与真实视觉模型识别主链路

#### 本次未完成内容

- 未完成 duijieai 平台真实实连与响应契约验证，当前默认仍走 fallback
- 未完成安卓端模拟器/真机“首页 -> 拍照 -> 识别结果 -> 保存 -> 首页刷新”手工联调
- 未完成人工修正增强交互、搜索扩展、删除记录、体重记录与生理期能力

#### 本次暴露的缺陷或偏差

- 安卓端本轮代码虽已落地，但当前机器上的 Gradle native service 初始化失败，导致 `:app:assembleDebug` 尚未完成本轮构建验证
- duijieai 平台大概率仍需你按真实平台文档手动调整请求体、鉴权与响应解析，当前仓库只保证保留了可运行 fallback 和清晰的修改入口

#### 这些缺陷 / 未完成项将在第几步收口

- 下一步：先在可用环境补安卓构建验证与最小手工联调
- 随后：按真实 duijieai 平台契约修改 `AdaptivePhotoAiRecognitionClient`，再补一次后端接口实调
- 后续业务轮次：再进入人工修正增强交互、搜索扩展和体重记录

#### 是否需要更新 spec

- 是

#### 备注

- 本轮只补齐拍照识别主链路直接需要的最小能力，没有扩展搜索页、删除记录、体重记录、生理期与复杂运营配置
- 后端 AI 适配已保留 fallback，确保即使平台接不通也不会阻塞当前主链路开发

### 2026-04-11 / 共享库初始化与后端接口实调

#### 本次主要完成内容

- 已修正后端联调阶段暴露的两个配置问题：
  - `MybatisPlusConfiguration` 的 `@MapperScan` 范围从整个 `modules` 收紧到各模块 `mapper` 包，避免把 Service 接口错误注册为 Mapper
  - `application.yml` 增加 `spring.sql.init.schema-locations=classpath:db/schema.sql`
- 已修正共享库种子数据回写缺口：
  - `schema.sql` 的 `food_library` upsert 新增 `food_name = VALUES(food_name)`，使重复初始化时能修正已存在种子中文名
  - `application.yml` 增加 `spring.sql.init.encoding=UTF-8`
- 已使用 `local` 配置连接共享库 `dev_cse_finance` 并通过 `spring.sql.init.mode=always` 执行最小表基线初始化
- 已完成后端真实启动与接口 smoke test：
  - `POST /goal/get`
  - `POST /goal/save`
  - `POST /diet/photo/upload`
  - `POST /diet/record/save`
  - `POST /diet/record/list`
  - `POST /diet/stat/today`
- 已确认最终 smoke test 结果：
  - `goal/get` 与 `goal/save` 返回 `code = 0`
  - 上传接口返回真实 `photoUrl`，识别项中文名已恢复为 `鸡蛋 / 番茄 / 炒饭`
  - 保存记录成功后，列表最新摘要为 `鸡蛋、番茄、炒饭`
  - 今日汇总返回 `consumedCalories = 858`、`targetCalories = 1500`

#### 本次未完成内容

- 未执行安卓端模拟器/真机手工回归
- 未提供共享库中 smoke test 饮食记录的清理能力
- 未接入真实百度识别、搜索页、删除记录、体重记录与生理期能力

#### 本次暴露的缺陷或偏差

- 共享库 smoke test 会产生真实测试记录，本轮因删除接口不在范围内，只能接受写入痕迹
- 后端本地启动和接口实调已完成，但仍未覆盖安卓端实际页面跳转与交互链路

#### 这些缺陷 / 未完成项将在第几步收口

- 下一步：安卓端执行一次端到端手工联调，确认首页、拍照、识别结果和目标页的真实接口状态一致
- 后续能力轮次：补删除记录或测试数据清理能力，再继续真实 AI 与搜索

#### 是否需要更新 spec

- 否

#### 备注

- 本轮接口 smoke test 使用的是共享库 `dev_cse_finance`
- 本轮 smoke test 保持了目标配置原值回写，但新增了 3 条今日饮食记录用于验证保存、列表和汇总

### 2026-04-11 / 首页主链路最小真实化落地

#### 本次主要完成内容

- 已完成后端首页主链路最小真实化：
  - 恢复单数据源与 MyBatis-Plus 自动装配
  - 新增 `AppProperties`、上传静态资源映射与固定 `user_id = 1` 配置
  - 重写最小表基线 `user_profile`、`diet_record`、`diet_item`、`food_library`
  - 落地 `UserProfilePO`、`DietRecordPO`、`DietItemPO`、`FoodLibraryPO` 及对应 Mapper
  - 清退 `common/dto` 业务模型，改为模块内聚的 `DTO / VO / PO`
- 已完成后端真实接口实现：
  - `POST /goal/get`
  - `POST /goal/save`
  - `POST /diet/photo/upload`
  - `POST /diet/record/save`
  - `POST /diet/record/list`
  - `POST /diet/stat/today`
- 已完成安卓端真实接口替换：
  - `DietApiService` 改为 6 个强类型真实接口
  - `RealGoalRepository`、`RealDietRecordRepository`、`RealRecognitionRepository` 已替换旧 mock 语义
  - 首页总览已固定使用 `/diet/stat/today`
  - 目标页已改为真实读取与保存，当前体重只读展示
  - 拍照页已改为“示例 `sample_meal.png` 资源 -> multipart 上传 -> 后端返回识别结果”
  - 识别结果页保存后可通过真实接口刷新首页
- 已完成本轮机械验证：
  - 安卓 `:app:assembleDebug` 通过
  - 后端 `scripts/backend-mvn.ps1 -DskipTests compile` 通过
  - `scripts/verify-baseline.ps1` 通过

#### 本次未完成内容

- 未把 `schema.sql` 实际初始化到共享库 `dev_cse_finance`
- 未执行带共享库连接的后端启动与接口实调
- 未做模拟器/真机手工联调回归
- 未接入真实百度识别、搜索页、删除记录、体重记录与生理期能力

#### 本次暴露的缺陷或偏差

- 本轮虽已完成代码和构建层收口，但共享数据库是否已具备新表基线尚未确认，不能把远端联调视为完成
- 示例上传资源已改为真实 `png` 资源，但目前仍是占位样例图，视觉上不是正式食物照片
- 后端识别结果当前仍由最小食物种子 mock 生成，接口契约虽已预留扩展字段，但真实 AI 匹配尚未验证

#### 这些缺陷 / 未完成项将在第几步收口

- 下一轮联调收口：先初始化 `schema.sql` 到共享库，再启动后端并验证 6 个真实接口
- 联调通过后：补安卓端最小手工回归，确认“首页 -> 拍照 -> 识别结果 -> 保存 -> 首页刷新”整链路
- 后续业务轮次：再接真实百度识别、搜索页与体重记录

#### 是否需要更新 spec

- 否

#### 备注

- 本轮严格控制在首页主链路最小真实化范围内，没有扩展 `food/search`、`food/detail`、`diet/record/delete`、`weight/record`、`period/*`、CameraX 与真实百度外调
- 数据库真实密码仍只保留在本地忽略文件 `backend/src/main/resources/application-local.yml`，未进入仓库跟踪

### 2026-04-11 / 首页主链路最小真实化边界切换

#### 本次主要完成内容

- 已确认本轮执行目标从“前端 mock 视觉维护”切换到“首页主链路最小真实化”：
  - 首页总览真值固定由 `/diet/stat/today` 提供
  - 拍照页继续保留示例上传入口，不接 CameraX
  - 识别结果本轮仍由后端 mock，不调用真实百度接口
- 已确认本轮只做以下真实接口范围：
  - `POST /goal/get`
  - `POST /goal/save`
  - `POST /diet/photo/upload`
  - `POST /diet/record/save`
  - `POST /diet/record/list`
  - `POST /diet/stat/today`
- 已确认后端命名规范切换为：
  - 持久化对象统一 `PO`
  - 请求参数统一 `DTO`
  - 响应结果统一 `VO`
  - `BO` 仅在确有复杂内部聚合时才允许新增
- 已确认数据库命名基线切换为无 `t_` 前缀：
  - `user_profile`
  - `diet_record`
  - `diet_item`
  - `food_library`

#### 本次未完成内容

- 真实接口、真实数据源、安卓真实仓储替换仍未落地
- `feature_status.md` 仍未更新为本轮实现后的最终真值

#### 本次暴露的缺陷或偏差

- 当前 `spec.md` 仍禁止真实接口替换与后端联调，必须先改边界再动代码
- 当前后端接口契约和安卓网络模型都还是 mock 阶段形态，直接并行开发会互相踩踏

#### 这些缺陷 / 未完成项将在第几步收口

- 本轮实施第一步：重写 `spec.md` 当前软规范
- 本轮实施收口阶段：完成代码后回写 `feature_status.md` 真值并补完整开发记录

#### 是否需要更新 spec

- 是

#### 备注

- 本轮属于从 mock UI 阶段切换到首页主链路最小真实化，不做搜索、删除记录、体重记录、生理期、CameraX 与真实百度外调
- 数据库继续按单用户 MVP 约束处理，固定 `user_id = 1`

### 2026-04-11 / 首页返回与拍照页闪退修复

#### 本次主要完成内容

- 已修复目标页缺少显式返回动作的问题：
  - 公共标题区支持可选返回按钮
  - 目标页已接入返回首页/回退上一路径
- 已修复点击拍照页闪退问题：
  - 移除拍照页中对 `sample_meal.xml` 的 Compose `painterResource` 加载
  - 改为纯 Compose 占位绘制，避免运行时资源解析崩溃
- 已执行安卓 `:app:assembleDebug`，构建通过
- 已执行 `scripts/verify-baseline.ps1`，后端打包与安卓 Debug 构建通过

#### 本次未完成内容

- 未新增更多页面返回逻辑精修，本轮只修复目标页返回缺口
- 未做真机/模拟器手工点击回归截图，仅完成代码与构建层验证

#### 本次暴露的缺陷或偏差

- 本轮 UI 调整后缺少最小交互回归，导致目标页返回缺口与拍照页运行时崩溃未在上一轮提前暴露
- Compose 对 XML drawable 的支持边界不能按传统 View 经验直接假设，后续要优先使用可确认兼容的资源类型

#### 这些缺陷 / 未完成项将在第几步收口

- 后续前端 UI 轮次：补最小交互回归清单，至少覆盖首页跳转、目标返回、拍照进入和识别保存链路
- 若继续优化视觉：优先使用 Compose 原生绘制或已验证兼容的位图/Vector 资源

#### 是否需要更新 spec

- 否

#### 备注

- 本轮属于现有 mock MVP 链路内缺陷修复，不改变当前边界
- 目标页返回与拍照页进入均已恢复到可用状态

### 2026-04-11 / MVP 首页优先 UI 完善

#### 本次主要完成内容

- 已按首页优先策略完成安卓端全局少女粉 UI 语言收口：
  - 粉白渐变背景
  - 樱花粉主色与糖果橙强调色
  - 大圆角卡片、按钮、输入框与柔和阴影
  - 统一轻装饰标题区与底部导航视觉
- 已重点打磨首页：
  - 重做首页标题头部
  - 重做今日总览核心视觉卡
  - 收口目标锚点进度条视觉
  - 重做当天记录卡片层次、餐次图标与配色
  - 强化首页主拍照入口按钮
- 已对拍照页、识别结果页、目标页完成基础统一风格：
  - 保留原有结构与 mock 交互
  - 统一背景、卡片、按钮、输入区与标题区样式
- 本轮未引入整套第三方 UI 库，继续基于官方 Material 3 做主题与组件定制
- 已执行安卓 `:app:assembleDebug`，构建通过
- 已执行 `scripts/verify-baseline.ps1`，后端打包与安卓 Debug 构建通过

#### 本次未完成内容

- 未对拍照页、识别结果页、目标页做高精度原型还原
- 未接入 CameraX、人工修正、真实接口与后端联调
- 未处理搜索页视觉细化，本轮仍不纳入范围

#### 本次暴露的缺陷或偏差

- 首页已建立 design language，但其余页面目前仅完成基础统一，还未扩散到更细节层级
- 当前按钮、卡片与标题区已脱离默认 Material 观感，但仍没有引入更高辨识度的自定义插画与食物资源
- 前端整体仍是 mock 链路，当前视觉收口不能替代真实业务联调与字段语义收口

#### 这些缺陷 / 未完成项将在第几步收口

- 若继续做前端 UI：以后续页面精修轮次，把首页已定下的设计语言向拍照/识别结果/目标页进一步扩散
- 若回到业务主线：第 2 批先补食物库、菜品明细、数据源与真实查询能力
- 后续联调轮次：再将当前视觉层与真实接口状态管理对接

#### 是否需要更新 spec

- 是

#### 备注

- 本轮只做视觉层完善，不改 mock 数据、不改后端、不改原始设计文档
- 本轮完成标准是“首页风格定住，其余三页不突兀且同一套设计语言”，不是四页全部高还原原型

### 2026-04-10 / 通用组件选型规范补充

#### 本次主要完成内容

- 新增实现规范：涉及通用组件时，必要情况下优先评估开源社区成熟方案，再决定是否手写
- 已将该规则写入 `spec.md` 硬命令，作为后续实现边界

#### 本次未完成内容

- 未建立具体的开源组件筛选清单

#### 本次暴露的缺陷或偏差

- 当前仓库还没有“组件优先复用”的固定选型流程，暂时仍依赖实现前人工判断

#### 这些缺陷 / 未完成项将在第几步收口

- 后续遇到通用组件需求时，先做开源方案评估，再决定是否引入或手写

#### 是否需要更新 spec

- 是

#### 备注

- 该规则优先作用于通用 UI 组件、通用业务组件和成熟基础能力组件

### 2026-04-10 / 前端优先 MVP 完成

#### 本次主要完成内容

- 已完成安卓首页、拍照、识别结果、目标页的 mock 链路实现
- 已完成前端 `AppContainer + mock repository + ViewModel + UiState` 轻量状态结构
- 已移除底部导航中的搜索入口，识别结果保留为流程页
- 已接入本地示例餐图、mock 识别结果、确认保存后首页刷新
- 已接入目标设置 mock 保存，并能同步首页目标热量展示
- 已完成安卓 `:app:assembleDebug` 构建验证

#### 本次未完成内容

- 未完成后端接口实现与真实联调
- 未完成搜索页接入主链路
- 未完成 CameraX、人工修正结果、真实图片上传

#### 本次暴露的缺陷或偏差

- 原设计顺序先做后端，但当前执行改为前端优先，需要靠 `spec.md` 维持边界
- 当前前端是纯 mock 链路，真实后端语义和字段映射仍待后续收口
- 当前 mock 数据仅在会话内有效，App 重启后会重置

#### 这些缺陷 / 未完成项将在第几步收口

- 第 2 批：补食物库、菜品明细、真实查询与数据源恢复
- 第 3 到第 5 批：补图片上传、识别、记录、目标等真实后端能力
- 后续联调轮次：将 mock repository 平滑替换为真实接口实现

#### 是否需要更新 spec

- 是

#### 备注

- 本轮仍不修改原始设计文档
- 本轮完成标准是“前端 mock MVP 可交互且可构建”，不是业务全量完成

### 2026-04-10 / Harness 边界收紧

#### 本次主要完成内容

- 保留 `feature_status.md` 作为独立完成度真值文件
- 去掉对进度标注版的依赖，当前 harness 不再以该文件作为执行入口
- 明确 `减肥计划拍照识别软件开发设计文档.md` 为原始设计基线，只读
- 更新 `AGENTS.md`、`spec.md`、`README.md`，统一上述边界

#### 本次未完成内容

- 未把“设计文档只读”升级为文件系统级只读属性
- 未对 `feature_status.md` 做更细粒度拆分

#### 本次暴露的缺陷或偏差

- 当前“设计文档只读”仍主要依赖文档规则，而不是系统级保护
- `feature_status.md` 仍是阶段级真值，后续可能还需收细

#### 这些缺陷 / 未完成项将在第几步收口

- 后续稳定期：若反复触发误改风险，再考虑加只读属性或脚本校验
- 第 2 批推进期：视真实实现复杂度，再决定是否把 `feature_status.md` 细化到接口级

#### 是否需要更新 spec

- 是

#### 备注

- 本轮只收紧 harness 边界，不改变当前第 2 批业务目标

### 2026-04-10 / Harness 最小控制层

#### 本次主要完成内容

- 新增 `AGENTS.md` 作为仓库级最小入口，固定阅读顺序与执行顺序
- 新增 `feature_status.md` 作为阶段完成度真值文件
- 新增 `scripts/verify-baseline.ps1` 作为最小机械验证入口
- 已实际执行 `scripts/verify-baseline.ps1`，后端打包与安卓 Debug 构建均通过
- 更新 `README.md`，把 harness 入口与启动顺序补到仓库首页

#### 本次未完成内容

- 未把设计文档第 13 节逐项拆成更细粒度的结构化真值
- 未把高频边界错误升级成自动化 lint 或结构检查
- 未把第 2 批业务完成判定接入脚本化验证

#### 本次暴露的缺陷或偏差

- 当前 harness 仍以文档约束为主，机械约束仍偏弱
- `feature_status.md` 目前是阶段级真值，还不是接口级或任务级真值
- `scripts/verify-baseline.ps1` 只验证构建，不验证业务接口是否真实可用

#### 这些缺陷 / 未完成项将在第几步收口

- 下一轮 harness 收口：把第 2 批完成判定补成更细的真实检查项
- 第 2 批业务实现轮次：补 `/food/search`、`/food/detail` 的真实查询与最小验证
- 后续稳定期：视重复错误情况，再决定是否补自动化 guardrail

#### 是否需要更新 spec

- 否

#### 备注

- 本轮新增的是执行控制层，不改变当前业务边界
- 当前业务推进仍以第 2 批为唯一主线

### 2026-04-10 / 初始化记录

#### 本次主要完成内容

- 已完成后端基础架构搭建：
  - Spring Boot 3.x 基础工程
  - 统一返回结构
  - 全局异常处理
  - 基础配置文件
  - 按文档接口路由创建后端占位 Controller
  - 数据库基础表结构 SQL
- 已完成安卓基础架构搭建：
  - Kotlin + Jetpack Compose 工程骨架
  - Navigation Compose 导航宿主
  - Retrofit2 + OkHttp 网络层接口声明
  - 首页、拍照页、识别结果页、搜索页、目标页占位页面
- 已完成本地开发环境准备：
  - 项目内 JDK 17
  - 项目内 Gradle 9.3.1
  - 项目内 Android SDK Command-line Tools
  - Android SDK Platform 36 / Build-Tools 36.0.0 / Platform-Tools
  - 后端和安卓分别使用项目内 JDK 17，不覆盖系统默认 JDK 8
- 已完成构建验证：
  - 后端 `mvn -DskipTests package` 成功
  - 安卓 `:app:assembleDebug` 成功

#### 本次未完成内容

- 未完成第 2 批食物库与菜品明细的真实业务实现
- 未完成第 3 批图片上传与 AI 识别
- 未完成第 4 批饮食记录主流程
- 未完成第 5 批目标与体重能力的真实接口能力
- 未完成安卓端 ViewModel、Repository、StateFlow 和真实交互态
- 未完成前后端联调

#### 本次暴露的缺陷或偏差

- 第 2 到第 5 批后端业务尚未实现，但第 6 批安卓骨架已经先落地
- 第 7 到第 9 批相关页面已经有占位结构，但不应视为业务完成
- 后端多个接口已建路由，但当前仍返回 `NOT_IMPLEMENTED`
- 后端为了让空库阶段先跑通骨架，临时排除了真实数据源与 MyBatis-Plus 自动装配

#### 这些缺陷 / 未完成项将在第几步收口

- 第 2 批：补实体、Mapper、Service、VO、食物库初始数据、真实查询接口
- 第 3 批：补图片上传落盘、Nginx 路径约定、百度 AI 接入与匹配逻辑
- 第 4 批：补饮食记录 CRUD、明细入库、今日汇总与主链路打通
- 第 5 批：补目标获取/保存、体重记录与首页数据联动
- 第 6 到第 9 批后续轮次：将安卓占位结构替换为真实状态管理、接口调用和联调能力

#### 是否需要更新 spec

- 是

#### 备注

- 本轮完成的是“基础架构 + 环境 + 可构建验证”，不是业务实现完成
- 下一轮必须以第 2 批为中心推进，禁止继续跨到 AI、上传、联调和安卓深化能力
