# 快速参考手册

## 🚀 一键启动

```bash
# 1. 启动数据库
docker start docker-db-1

# 2. 启动后端（IDEA 或命令行二选一）
cd devinsight-backend && mvn spring-boot:run

# 3. 测试服务
curl http://localhost:8080/health
```

---

## 📡 API 速查

### 基础信息
- **Base URL**: `http://localhost:8080`
- **认证方式**: `Authorization: Bearer <token>`

### 常用接口

#### 快速测试流程
```bash
# 1. 注册
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"123456","email":"demo@example.com"}'

# 2. 登录（获取 token）
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"123456"}' \
  | jq -r '.data.token')

echo "Token: $TOKEN"

# 3. 创建项目
curl -X POST http://localhost:8080/api/projects \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"测试项目","description":"快速测试"}'

# 4. 查看项目
curl http://localhost:8080/api/projects \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🗄️ 数据库速查

### 连接数据库
```bash
docker exec -it docker-db-1 psql -U devinsight_user -d devinsight
```

### 常用查询
```sql
-- 查看所有表
\dt

-- 查看用户
SELECT id, username, email, created_at FROM "user";

-- 查看项目
SELECT id, name, owner_id, created_at FROM project;

-- 查看项目成员关系
SELECT pm.project_id, p.name, u.username, pm.role 
FROM project_member pm
JOIN project p ON pm.project_id = p.id
JOIN "user" u ON pm.user_id = u.id;

-- 清空测试数据
TRUNCATE TABLE project_member, project, "user" CASCADE;
```

### 数据库操作
```bash
# 启动
docker start docker-db-1

# 停止
docker stop docker-db-1

# 查看日志
docker logs docker-db-1

# 备份数据库
docker exec docker-db-1 pg_dump -U devinsight_user devinsight > backup.sql

# 恢复数据库
docker exec -i docker-db-1 psql -U devinsight_user -d devinsight < backup.sql
```

---

## 🛠️ 常用命令

### Maven
```bash
# 清理 + 编译
mvn clean compile

# 运行
mvn spring-boot:run

# 打包
mvn clean package

# 跳过测试打包
mvn clean package -DskipTests

# 查看依赖树
mvn dependency:tree
```

### Docker
```bash
# 查看运行中的容器
docker ps

# 查看所有容器
docker ps -a

# 启动容器
docker start docker-db-1

# 停止容器
docker stop docker-db-1

# 查看容器日志
docker logs -f docker-db-1

# 进入容器
docker exec -it docker-db-1 /bin/sh
```

### Git
```bash
# 查看状态
git status

# 添加文件
git add .

# 提交
git commit -m "feat: 添加用户认证功能"

# 推送
git push origin main

# 拉取
git pull origin main

# 查看日志
git log --oneline
```

---

## 🔧 配置速查

### application.yml
```yaml
# 端口
server:
  port: 8080

# 数据库
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/devinsight
    username: devinsight_user
    password: devinsight123

# JWT
jwt:
  secret: your-secret-key
  expiration: 86400000  # 24小时
```

### 环境变量覆盖
```bash
# 临时修改端口
SERVER_PORT=8081 mvn spring-boot:run

# 临时修改数据库
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/devinsight_test
```

---

## 🐛 问题排查

### 服务启动失败

#### 1. 检查端口占用
```bash
lsof -i :8080
kill -9 <PID>
```

#### 2. 检查数据库连接
```bash
docker ps | grep postgres
docker start docker-db-1
```

#### 3. 查看应用日志
```bash
# IDEA 控制台
# 或命令行输出
```

### API 调用失败

#### 1. 401 未授权
```bash
# 检查 token 是否携带
# 检查 token 是否过期（24小时）
# 重新登录获取新 token
```

#### 2. 403 无权限
```bash
# 检查用户是否为项目成员
# 检查操作是否需要 OWNER 权限
```

#### 3. 500 服务器错误
```bash
# 查看控制台日志
# 检查数据库连接
# 检查 SQL 语句
```

---

## 📝 代码片段

### 创建新的 Controller
```java
@RestController
@RequestMapping("/api/xxx")
public class XxxController {
    
    @Autowired
    private XxxService xxxService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", xxxService.list(userId));
        return ResponseEntity.ok(result);
    }
}
```

### 创建新的 Service
```java
@Service
public class XxxService {
    
    @Autowired
    private XxxMapper xxxMapper;
    
    public List<Xxx> list(Long userId) {
        LambdaQueryWrapper<Xxx> query = new LambdaQueryWrapper<>();
        query.eq(Xxx::getUserId, userId);
        return xxxMapper.selectList(query);
    }
}
```

### 创建新的实体
```java
@Data
@TableName("xxx")
public class Xxx {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private LocalDateTime createdAt;
}
```

---

## 🎯 下一步开发

### Phase 3: AI 核心能力

#### 任务队列表
```sql
CREATE TABLE ai_task (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    task_type VARCHAR(50) NOT NULL,  -- EXCEPTION_ANALYSIS, CODE_DIFF
    status VARCHAR(20) NOT NULL,      -- PENDING, PROCESSING, COMPLETED, FAILED
    input_data TEXT,
    result_data TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);
```

#### API 设计
```
POST /api/ai/analyze-exception
POST /api/ai/analyze-code-diff
GET  /api/ai/tasks
GET  /api/ai/tasks/{id}
```

---

## 📚 学习资源

### Spring Boot
- [官方文档](https://spring.io/projects/spring-boot)
- [Spring Boot 3.x 新特性](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Release-Notes)

### MyBatis-Plus
- [官方文档](https://baomidou.com/)
- [快速开始](https://baomidou.com/pages/226c21/)

### JWT
- [JWT.io](https://jwt.io/)
- [JJWT GitHub](https://github.com/jwtk/jjwt)

### PostgreSQL
- [官方文档](https://www.postgresql.org/docs/)
- [Docker Hub](https://hub.docker.com/_/postgres)

---

**提示**: 将此文件添加到浏览器书签，方便随时查阅！
