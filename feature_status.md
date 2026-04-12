# Feature Status

本文文件是真实完成度真值。占位、骨架、空返回、未联调能力，不得记为完成。

| 阶段 | 状态 | 当前真值 |
| --- | --- | --- |
| 第 0 批：冻结开发边界 | done | 已完成 |
| 第 1 批：后端基础骨架 | done | 已完成 |
| 第 2 批：食物库与菜品明细 | pending | 实体、Mapper、Service、真实查询均未完成 |
| 第 3 批：图片上传与 AI 识别 | partial | `/diet/photo/upload`、图片真实读取、按需压缩、处理后落盘、安卓真实拍照上传已落地；后端识别层当前按 MVP 收口为“`GLM-5V-Turbo` 主识别 + 本地 fallback”，并保留少量 provider 适配点以承接后续多模型接入；上传接口在保留 `recognizedItems` 的同时，已新增结构化 `structuredResult` 返回；`Glm46VAdaptiveRecognitionDemoTest` 已使用 `tools/picture` 下真实餐食图跑通，当前能返回麻辣烫及更细的食材明细；百度能力代码仍保留在仓库中，但当前默认配置已关闭实际调用 |
| 第 4 批：饮食记录主流程 | partial | `/diet/record/save`、`/diet/record/list`、`/diet/stat/today` 与 `diet_record / diet_item` 入库代码已落地，首页主链路前后端已切到真实接口；共享库初始化与接口实调已完成，删除记录未做 |
| 第 5 批：目标与体重能力 | partial | `/goal/get`、`/goal/save` 与目标页真实读写已落地；`weight/record` 仍未实现 |
| 第 6 批：安卓端基础框架 | done | 导航、ViewModel、StateFlow、真实 Retrofit 服务、真实 repository 与公共组件已落地，Debug 构建通过 |
| 第 7 批：首页与搜索页 | partial | 首页总览与记录列表已改为真实接口驱动，首页 UI 保持统一风格；搜索页按本轮范围未接入 |
| 第 8 批：拍照与识别结果页 | partial | CameraX 预览、拍照、真实图片上传、后端识别结果展示、确认保存回首页刷新已接入主链路；人工修正交互未做，安卓端手工联调未完成 |
| 第 9 批：目标设置页与联调 | partial | 目标页已改为真实读取和保存，当前体重已支持随目标一并保存；共享库数据库初始化、后端启动与 6 个真实接口实调已完成，安卓端手工联调未做 |
| 第 10 批：收尾 | partial | 后端 compile 已通过；安卓端 `:app:assembleDebug` 已重新验证通过；共享库实连验证已完成，安卓端整链路手工回归未做；后端已补中文注释基线、logback 日志配置与 hutool 依赖占位；安卓端已补结构化中文注释与关键状态日志；百度菜品识别 Demo 单测已使用真实凭证和仓库占位图完成一次实调验证 |

## 当前下一步

- 当前应执行：补一次安卓模拟器或真机“首页 -> 拍照 -> 识别结果 -> 保存 -> 首页刷新”手工联调，并决定前端是否需要消费 `structuredResult` 做食材级展示
- 当前阻塞点：安卓端端到端手工回归尚未完成；前端当前仍只消费 `recognizedItems`，尚未展示新的结构化识别结果
