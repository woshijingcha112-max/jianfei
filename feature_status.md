# Feature Status

本文件是真实完成度真值。占位、骨架、空返回、未联调能力，不得记为完成。

| 阶段 | 状态 | 当前真值 |
| --- | --- | --- |
| 第 0 批：冻结开发边界 | done | 已完成 |
| 第 1 批：后端基础骨架 | done | 已完成 |
| 第 2 批：食物库与菜品明细 | pending | 实体、Mapper、Service、真实查询均未完成 |
| 第 3 批：图片上传与 AI 识别 | partial | `/diet/photo/upload`、图片落盘、静态访问映射、安卓真实 multipart 上传已落地；识别结果当前仍为后端 mock，未接真实百度接口 |
| 第 4 批：饮食记录主流程 | partial | `/diet/record/save`、`/diet/record/list`、`/diet/stat/today` 与 `diet_record / diet_item` 入库代码已落地，首页主链路前后端已切到真实接口；共享库初始化与接口实调已完成，删除记录未做 |
| 第 5 批：目标与体重能力 | partial | `/goal/get`、`/goal/save` 与目标页真实读写已落地；`weight/record` 仍未实现 |
| 第 6 批：安卓端基础框架 | done | 导航、ViewModel、StateFlow、真实 Retrofit 服务、真实 repository 与公共组件已落地，Debug 构建通过 |
| 第 7 批：首页与搜索页 | partial | 首页总览与记录列表已改为真实接口驱动，首页 UI 保持统一风格；搜索页按本轮范围未接入 |
| 第 8 批：拍照与识别结果页 | partial | 示例餐图资源上传、后端识别结果展示、确认保存回首页刷新已改为真实链路；未接 CameraX 与人工修正 |
| 第 9 批：目标设置页与联调 | partial | 目标页已改为真实读取和保存，当前体重后端只读展示；共享库数据库初始化、后端启动与 6 个真实接口实调已完成，安卓端手工联调未做 |
| 第 10 批：收尾 | partial | `:app:assembleDebug`、后端 compile、`scripts/verify-baseline.ps1` 已通过；共享库实连验证已完成，安卓端整链路手工回归未做 |

## 当前下一步

- 当前应执行：在安卓模拟器或真机上做一次“首页 -> 拍照 -> 识别结果 -> 保存 -> 首页刷新”手工联调；若继续扩能力，再进入真实 AI、搜索或体重记录
- 当前阻塞点：安卓端端到端手工回归尚未完成；本轮 smoke test 已向共享库写入测试记录
