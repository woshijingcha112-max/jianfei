# AGENTS

如有问题，先与用户确认。

## 先读顺序

1. `spec.md`
2. `feature_status.md`
3. `project_develop_log.md`
4. `减肥计划拍照识别软件开发设计文档.md`

## 文件职责

- `spec.md`：项目硬边界与稳定软规范，只负责“缰绳”，不承载项目进度。
- `feature_status.md`：真实完成度真值，不把占位当完成。
- `project_develop_log.md`：每轮开发后的复盘、阶段性策略、偏差与收口记录。
- `减肥计划拍照识别软件开发设计文档.md`：原始设计基线，只读。
- `scripts/verify-baseline.ps1`：最小机械验证入口。

## 最小执行流程

1. 开工前先读 `spec.md`硬边界部分，确认当前任务未越界。
2. 开工前再读 `spec.md`软规范部分，确认当前稳定工程约束、注释日志要求与复用策略。
3. 再读 `feature_status.md`，确认当前阶段真实完成度。
4. 如本轮需要临时策略、阶段性折中或试验性方案，先写入 `project_develop_log.md`，不要直接写进 `spec.md`。
5. 只有当某项规则已经沉淀为稳定约束时，才允许更新 `spec.md` 软规范。
6. 实现时只做当前任务直接需要的最小改动。
7. 只有在需要确认原始设计时才读 `减肥计划拍照识别软件开发设计文档.md`，且不得修改。
8. 完成后执行 `scripts/verify-baseline.ps1` 或本轮最小相关验证。
9. 完成后更新 `project_develop_log.md`；只有真实完成度发生变化时才更新 `feature_status.md`；只有边界或稳定软规范变化时才更新 `spec.md`。
