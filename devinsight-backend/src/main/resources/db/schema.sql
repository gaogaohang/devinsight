-- ============================================
-- DevInsight 数据库初始化脚本
-- 版本: 1.0.0
-- 创建时间: 2026-02-01
-- 描述: 用户认证与项目管理核心表结构
-- ============================================

-- 1. 用户表（系统用户）
-- 注意: 使用 sys_user 避免与 PostgreSQL 保留字 "user" 冲突
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON COLUMN sys_user.password IS 'BCrypt 加密密码';

-- 用户表索引
CREATE INDEX IF NOT EXISTS idx_user_username ON sys_user(username);
CREATE INDEX IF NOT EXISTS idx_user_email ON sys_user(email);

-- 2. 项目表
CREATE TABLE IF NOT EXISTS project (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    owner_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_project_owner FOREIGN KEY (owner_id) REFERENCES sys_user(id) ON DELETE CASCADE
);

-- 项目表索引
CREATE INDEX IF NOT EXISTS idx_project_owner ON project(owner_id);
CREATE INDEX IF NOT EXISTS idx_project_name ON project(name);

-- 3. 项目成员关系表（多对多关系）
CREATE TABLE IF NOT EXISTS project_member (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('OWNER', 'MEMBER')),
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_member_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    CONSTRAINT fk_member_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT uk_project_user UNIQUE(project_id, user_id)
);

-- 项目成员表索引
CREATE INDEX IF NOT EXISTS idx_project_member_project ON project_member(project_id);
CREATE INDEX IF NOT EXISTS idx_project_member_user ON project_member(user_id);
CREATE INDEX IF NOT EXISTS idx_project_member_role ON project_member(role);

-- 4. AI 任务表
CREATE TABLE IF NOT EXISTS ai_task (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    task_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('pending', 'running', 'success', 'failed')),
    input_data TEXT,
    result_data TEXT,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT fk_task_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
);

-- AI 任务表索引
CREATE INDEX IF NOT EXISTS idx_task_project ON ai_task(project_id);
CREATE INDEX IF NOT EXISTS idx_task_user ON ai_task(user_id);
CREATE INDEX IF NOT EXISTS idx_task_status ON ai_task(status);
CREATE INDEX IF NOT EXISTS idx_task_type ON ai_task(task_type);
CREATE INDEX IF NOT EXISTS idx_task_created ON ai_task(created_at DESC);

-- ============================================
-- 表结构说明
-- ============================================
-- sys_user: 系统用户表
--   - id: 主键（自增）
--   - username: 用户名（唯一）
--   - password: BCrypt 加密的密码
--   - email: 邮箱（唯一）
--   - created_at/updated_at: 创建和更新时间
--
-- project: 项目表
--   - id: 主键（自增）
--   - name: 项目名称
--   - description: 项目描述
--   - owner_id: 项目所有者（外键关联 sys_user.id）
--   - created_at/updated_at: 创建和更新时间
--
-- project_member: 项目成员关系表
--   - id: 主键（自增）
--   - project_id: 项目 ID（外键）
--   - user_id: 用户 ID（外键）
--   - role: 角色（OWNER/MEMBER）
--   - joined_at: 加入时间
--   - 唯一约束: (project_id, user_id)
--
-- ai_task: AI 任务表
--   - id: 主键（自增）
--   - project_id: 所属项目（外键）
--   - user_id: 提交用户（外键）
--   - task_type: 任务类型（EXCEPTION_ANALYSIS/LOG_SUMMARY）
--   - status: 任务状态（pending/running/success/failed）
--   - input_data: 输入数据（JSON 格式）
--   - result_data: 结果数据（JSON 格式）
--   - error_message: 失败原因
--   - created_at: 创建时间
--   - started_at: 开始执行时间
--   - completed_at: 完成时间
-- ============================================

-- ============================================
-- 关系说明
-- ============================================
-- 1. sys_user 1:N project
--    一个用户可以创建多个项目
--
-- 2. sys_user M:N project (通过 project_member)
--    一个用户可以参与多个项目
--    一个项目可以有多个成员
--
-- 3. 级联删除规则
--    - 删除用户 → 删除其创建的所有项目 → 删除所有项目成员记录和任务
--    - 删除项目 → 删除所有项目成员记录和任务
--
-- 4. ai_task 任务生命周期
--    pending → running → success/failed
--    - pending: 等待执行
--    - running: 正在执行
--    - success: 执行成功（有 result_data）
--    - failed: 执行失败（有 error_message）
-- ============================================
