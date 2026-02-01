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

- [x] **Phase 1: 基础项目结构**
  - [x] Spring Boot 3.3.0 + Java 17
  - [x] PostgreSQL 数据库（Docker）
  - [x] 健康检查接口
- [x] **Phase 2: 用户认证与项目管理**
  - [x] 用户注册/登录
  - [x] JWT Token 认证
  - [x] 项目 CRUD 操作
  - [x] 基于角色的权限控制（OWNER/MEMBER）
- [x] **Phase 2: AI 任务系统**
  - [x] 任务提交与状态管理
  - [x] 定时扫描执行器（数据库轮询 + 乐观锁）
  - [x] 任务生命周期追溯（pending → running → success/failed）
  - [x] 模拟 AI 处理（异常分析、日志总结）
- [ ] **Phase 3: AI 集成**
  - [ ] Python FastAPI 服务
  - [ ] 对接免费 AI API
  - [ ] Backend ↔ AI Core 通信
- [ ] 代码变更影响分析

---

## 规划功能

- AI 异常分析
- Git Diff 影响分析
- Prompt 版本管理
- 成本与稳定性控制

---

## 快速开始

### 1. 启动数据库

```bash
# 启动 PostgreSQL 容器
docker start docker-db-1

# 或创建新容器
docker run -d \
  --name docker-db-1 \
  -e POSTGRES_DB=devinsight \
  -e POSTGRES_USER=devinsight_user \
  -e POSTGRES_PASSWORD=devinsight123 \
  -p 5432:5432 \
  postgres:15-alpine
```

### 2. 初始化数据库

```bash
# 进入数据库容器
docker exec -it docker-db-1 psql -U devinsight_user -d devinsight

# 执行初始化脚本（在 psql 中）
\i /path/to/devinsight-backend/src/main/resources/db/schema.sql

# 或手动复制粘贴 schema.sql 内容执行
```

### 3. 启动后端服务

```bash
cd devinsight-backend
mvn spring-boot:run
```

服务将在 `http://localhost:8080` 启动

### 4. 测试接口

```bash
# 健康检查
curl http://localhost:8080/health

# 或运行完整测试脚本
./test-api.sh
```

---

## 项目结构

```
devinsight/
├── devinsight-backend/          # Spring Boot 后端
│   └── src/main/resources/db/   # 数据库脚本
│       └── schema.sql           # 表结构初始化
├── docs/                        # 项目文档
│   ├── README.md                # 文档总览
│   ├── phase-1-basic-setup.md   # 第一阶段：基础搭建
│   ├── phase-2-user-project-management.md  # 第二阶段：用户与项目管理
│   └── quick-reference.md       # 快速参考手册
├── test-api.sh                  # API 测试脚本
└── README.md                    # 项目说明
```

---

## 技术栈

### Backend
- Spring Boot 3.3.0
- Java 17
- MyBatis-Plus 3.5.7
- PostgreSQL 15
- JWT 认证
- BCrypt 密码加密

### 数据库
- PostgreSQL 15 (Docker)
- 核心表：`sys_user`, `project`, `project_member`

---

## API 接口

### 认证相关
| 接口 | 方法 | 说明 | 鉴权 |
|------|------|------|------|
| `/health` | GET | 健康检查 | ❌ |
| `/api/auth/register` | POST | 用户注册 | ❌ |
| `/api/auth/login` | POST | 用户登录 | ❌ |
| `/api/users/me` | GET | 获取当前用户 | ✅ |

### 项目管理
| 接口 | 方法 | 说明 | 鉴权 |
|------|------|------|------|
| `/api/projects` | GET | 获取我的项目 | ✅ |
| `/api/projects` | POST | 创建项目 | ✅ |
| `/api/projects/{id}` | GET | 项目详情 | ✅ 成员 |
| `/api/projects/{id}` | PUT | 更新项目 | ✅ OWNER |
| `/api/projects/{id}` | DELETE | 删除项目 | ✅ OWNER |

### AI 任务系统
| 接口 | 方法 | 说明 | 鉴权 |
|------|------|------|------|
| `/api/tasks` | POST | 提交任务 | ✅ |
| `/api/tasks/{id}` | GET | 任务详情 | ✅ 项目成员 |
| `/api/tasks/my` | GET | 我的任务列表 | ✅ |
| `/api/tasks/project/{projectId}` | GET | 项目任务列表 | ✅ 项目成员 |

详细文档请查看 [`docs/`](docs/) 目录

---

## 说明

本项目为个人工程实践项目，持续演进中。
