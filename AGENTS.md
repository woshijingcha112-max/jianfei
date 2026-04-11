# AGENTS

如有问题，先与用户确认。

## 先读顺序

1. `spec.md`
2. `feature_status.md`
3. `project_develop_log.md`
4. `减肥计划拍照识别软件开发设计文档.md`

## 文件职责

- `spec.md`：当前硬边界与软规范。
- `feature_status.md`：真实完成度真值，不把占位当完成。
- `project_develop_log.md`：每轮开发后的复盘与收口记录。
- `减肥计划拍照识别软件开发设计文档.md`：原始设计基线，只读。
- `scripts/verify-baseline.ps1`：最小机械验证入口。

## 最小执行流程

1. 开工前先读 `spec.md`硬边界部分，确认当前任务未越界。
2. 开工前再读 `feature_status.md`，确认当前阶段真实完成度。
2. 通过`spec.md`硬边界部分 + 当前迭代的需求重构软规范部分。
3. 实现时只做当前任务直接需要的最小改动。
4. 只有在需要确认原始设计时才读 `减肥计划拍照识别软件开发设计文档.md`，且不得修改。
5. 完成后执行 `scripts/verify-baseline.ps1` 或本轮最小相关验证。
6. 完成后先更新 `feature_status.md` 和 `project_develop_log.md`；只有边界变化时才改 `spec.md`。
