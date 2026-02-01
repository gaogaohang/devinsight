# DevInsight

DevInsight 是一个面向研发团队的 AI 辅助研发平台，
用于代码变更影响分析、异常/日志智能总结和技术文档生成，
通过大模型能力降低研发沟通与排错成本。

---

## 背景

在实际软件研发过程中，以下问题普遍存在：

- 代码变更后，影响范围难以快速判断
- 异常日志分散，排查成本高
- 技术文档与代码长期不一致

DevInsight 尝试引入大模型能力，结合工程化设计，
构建一个可落地的 AI 研发辅助系统。

---

## 整体架构

- **devinsight-backend**
  - Java / Spring Boot
  - 负责用户、项目、任务调度与权限控制

- **devinsight-ai-core**
  - Python / FastAPI
  - 负责大模型调用、Prompt 管理与结果生成

- **基础设施**
  - Redis：缓存、限流
  - PostgreSQL：核心数据
  - Docker Compose：统一部署

---

## 当前进度

- [ ] 基础项目结构
- [ ] Backend ↔ AI Core 通信
- [ ] 异常 / 日志智能总结
- [ ] 代码变更影响分析

---

## 规划功能

- AI 异常分析
- Git Diff 影响分析
- Prompt 版本管理
- 成本与稳定性控制

---

## 说明

本项目为个人工程实践项目，持续演进中。
