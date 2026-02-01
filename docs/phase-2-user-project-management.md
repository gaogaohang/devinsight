# Phase 2: 用户与项目管理

## 🎯 阶段目标

实现基础的用户认证和项目管理能力，为后续所有 AI 功能提供归属与隔离基础。

## 📋 需求清单

### 功能要求
- ✅ 用户注册 / 登录
- ✅ 项目创建
- ✅ 项目与用户绑定
- ✅ 简单角色控制（owner / member）

### 边界说明
- ⛔ 不做复杂权限模型（RBAC）
- ⛔ 不做组织 / 团队树
- ⛔ 不做 OAuth2 第三方登录
- ⛔ 不做密码找回功能

## 🏗️ 数据模型设计

### 1. 核心实体

#### User（用户表）
```sql
CREATE TABLE "user" (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,        -- BCrypt 加密
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Project（项目表）
```sql
CREATE TABLE project (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    owner_id BIGINT NOT NULL,              -- 外键关联 user.id
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES "user"(id) ON DELETE CASCADE
);
```

#### ProjectMember（项目成员关系表）
```sql
CREATE TABLE project_member (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,            -- 外键关联 project.id
    user_id BIGINT NOT NULL,               -- 外键关联 user.id
    role VARCHAR(20) NOT NULL,             -- OWNER / MEMBER
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    UNIQUE(project_id, user_id)
);
```

### 2. 关系说明
- `User` 1:N `Project`（一个用户可创建多个项目）
- `User` M:N `Project`（通过 `ProjectMember` 实现多对多）
- 创建项目时自动在 `ProjectMember` 中添加 OWNER 记录

## 🔧 技术方案

### 1. 新增依赖

#### MyBatis-Plus
- 版本: 3.5.5
- 简化数据库操作
- 内置分页、条件构造器

#### PostgreSQL
- 版本: 15-alpine (Docker)
- 端口: 5432
- 数据库: devinsight
- 用户: devinsight_user / devinsight123

#### JWT
- jjwt 0.12.5
- 无状态认证
- Token 有效期: 24小时

#### Lombok
- 简化实体类代码
- 自动生成 getter/setter

### 2. 认证流程

#### 注册流程
```
POST /api/auth/register
{
  "username": "user1",
  "password": "123456",
  "email": "user1@example.com"
}
↓
1. 校验用户名唯一性
2. 校验邮箱唯一性
3. BCrypt 加密密码
4. 保存到 User 表
5. 返回成功
```

#### 登录流程
```
POST /api/auth/login
{
  "username": "user1",
  "password": "123456"
}
↓
1. 查询用户
2. BCrypt 校验密码
3. 生成 JWT Token (payload: userId, username)
4. 返回 token 和用户信息
```

#### JWT Token 结构
```
Header: { "alg": "HS256", "typ": "JWT" }
Payload: {
  "sub": "1",                    // userId
  "username": "user1",
  "iat": 1706745600,             // 签发时间
  "exp": 1706832000              // 过期时间
}
Signature: HMACSHA256(...)
```

### 3. 权限控制

#### 拦截器机制
```java
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, ...) {
        // 1. 提取 Authorization Header
        // 2. 验证 JWT Token
        // 3. 提取 userId 存入 request.attribute
        // 4. 放行请求
    }
}
```

#### 白名单配置
```java
registry.addInterceptor(jwtAuthInterceptor)
    .addPathPatterns("/api/**")
    .excludePathPatterns(
        "/api/auth/register",
        "/api/auth/login",
        "/health"
    );
```

#### 角色校验逻辑
```java
// 在 Service 层检查权限
private void checkProjectOwner(Long userId, Long projectId) {
    // 查询 ProjectMember 表
    // 判断 role == OWNER
    // 否则抛 403 异常
}
```

## 📂 代码结构

```
com.devinsight/
├── controller/
│   ├── AuthController         # 注册/登录
│   ├── UserController         # 用户信息
│   └── ProjectController      # 项目 CRUD
├── service/
│   ├── AuthService           # 认证逻辑
│   ├── UserService           # 用户管理
│   └── ProjectService        # 项目管理 + 权限校验
├── mapper/
│   ├── UserMapper
│   ├── ProjectMapper
│   └── ProjectMemberMapper
├── entity/
│   ├── User
│   ├── Project
│   └── ProjectMember
├── dto/                       # 请求参数
│   ├── RegisterRequest
│   ├── LoginRequest
│   └── ProjectCreateRequest
├── vo/                        # 响应数据
│   ├── LoginResponse
│   └── ProjectVO
├── util/
│   └── JwtUtil               # JWT 工具类
├── interceptor/
│   └── JwtAuthInterceptor    # JWT 拦截器
├── config/
│   ├── SecurityConfig        # Security 配置
│   └── WebConfig             # 拦截器配置
└── exception/
    ├── BusinessException     # 业务异常
    └── GlobalExceptionHandler # 全局异常处理
```

## 🚀 核心流程

### 1. 创建项目流程
```
POST /api/projects
Header: Authorization: Bearer <token>
{
  "name": "AI 异常分析",
  "description": "生产环境日志分析"
}
↓
1. JWT 拦截器提取 userId
2. 创建 Project (owner_id = userId)
3. 创建 ProjectMember (role = OWNER)
4. 返回项目信息（包含 myRole）
```

### 2. 权限校验流程

#### 查看项目（成员权限）
```
GET /api/projects/{id}
↓
1. 提取 userId
2. 查询 ProjectMember 是否存在记录
3. 存在 → 返回项目信息
4. 不存在 → 403 Forbidden
```

#### 删除项目（OWNER 权限）
```
DELETE /api/projects/{id}
↓
1. 提取 userId
2. 查询 ProjectMember (role = OWNER)
3. 是 OWNER → 执行删除
4. 不是 → 403 Forbidden "仅限项目所有者"
```

## 📡 API 设计

### 认证相关

#### 1. 用户注册
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "test",
  "password": "123456",
  "email": "test@example.com"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "注册成功"
}
```

#### 2. 用户登录
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "test",
  "password": "123456"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "username": "test"
  }
}
```

### 用户相关

#### 3. 获取当前用户信息
```http
GET /api/users/me
Authorization: Bearer <token>
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "username": "test",
    "email": "test@example.com",
    "createdAt": "2026-02-01T10:00:00",
    "updatedAt": "2026-02-01T10:00:00"
  }
}
```

### 项目相关

#### 4. 创建项目
```http
POST /api/projects
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "AI异常分析",
  "description": "生产环境日志智能分析项目"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "name": "AI异常分析",
    "description": "生产环境日志智能分析项目",
    "ownerId": 1,
    "ownerName": "test",
    "myRole": "OWNER",
    "createdAt": "2026-02-01T10:00:00",
    "updatedAt": "2026-02-01T10:00:00"
  }
}
```

#### 5. 获取我的项目列表
```http
GET /api/projects
Authorization: Bearer <token>
```

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "AI异常分析",
      "description": "生产环境日志智能分析项目",
      "ownerId": 1,
      "ownerName": "test",
      "myRole": "OWNER",
      "createdAt": "2026-02-01T10:00:00",
      "updatedAt": "2026-02-01T10:00:00"
    }
  ]
}
```

#### 6. 获取项目详情
```http
GET /api/projects/{id}
Authorization: Bearer <token>
```

**权限要求**: 项目成员（OWNER 或 MEMBER）

#### 7. 更新项目
```http
PUT /api/projects/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "AI异常分析（已更新）",
  "description": "新的描述"
}
```

**权限要求**: OWNER

#### 8. 删除项目
```http
DELETE /api/projects/{id}
Authorization: Bearer <token>
```

**权限要求**: OWNER

## ✅ 验收标准

### 功能验收
- [x] 一个用户可以注册并登录
- [x] 登录后获得 JWT Token
- [x] 用户可以创建项目
- [x] 项目自动关联到创建者（OWNER 角色）
- [x] 可以查询我的项目列表
- [x] 只有 OWNER 可以更新/删除项目
- [x] 非项目成员无法访问项目

### 安全验收
- [x] 密码使用 BCrypt 加密存储
- [x] 未登录无法访问受保护接口（401）
- [x] 无权限无法操作资源（403）
- [x] JWT Token 过期自动失效

## 🧪 测试方式

### 自动化测试脚本
```bash
cd /Users/gh/devinsight
./test-api.sh
```

脚本会依次执行：
1. 健康检查
2. 用户注册
3. 用户登录
4. 获取用户信息
5. 创建项目
6. 查询项目列表
7. 查询项目详情
8. 更新项目
9. 再次查询验证

### 手动测试示例

#### 完整流程
```bash
# 1. 注册
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456","email":"test@example.com"}'

# 2. 登录
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}' \
  | jq -r '.data.token')

# 3. 创建项目
curl -X POST http://localhost:8080/api/projects \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"测试项目","description":"这是一个测试项目"}'

# 4. 查询项目
curl http://localhost:8080/api/projects \
  -H "Authorization: Bearer $TOKEN"
```

## 🔧 环境配置

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/devinsight
    username: devinsight_user
    password: devinsight123
```

### JWT 配置
```yaml
jwt:
  secret: devinsight-secret-key-change-in-production-123456789
  expiration: 86400000  # 24小时（毫秒）
```

### Docker PostgreSQL
```bash
# 启动容器
docker start docker-db-1

# 进入数据库
docker exec -it docker-db-1 psql -U devinsight_user -d devinsight

# 查看表
\dt

# 查看数据
SELECT * FROM "user";
SELECT * FROM project;
SELECT * FROM project_member;
```

## 📊 数据库索引

已创建的索引：
- `idx_project_owner` - project(owner_id)
- `idx_project_member_project` - project_member(project_id)
- `idx_project_member_user` - project_member(user_id)

优化查询性能：
- 快速查找用户的项目
- 快速查找项目的成员
- 快速验证用户权限

## 🎯 下一步规划

### Phase 3: AI 核心能力
- Backend ↔ AI Core 通信（HTTP/gRPC）
- 异常/日志智能总结
- Prompt 管理
- 任务队列与调度

### 未来扩展
- 添加成员到项目（POST /api/projects/{id}/members）
- 移除项目成员（DELETE /api/projects/{id}/members/{userId}）
- 项目成员列表（GET /api/projects/{id}/members）
- 转让项目所有权

---

**完成时间**: 2026-02-01  
**数据库**: PostgreSQL 15 (Docker)  
**认证方式**: JWT Token  
**开发工具**: IntelliJ IDEA + Maven
