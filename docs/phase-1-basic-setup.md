# Phase 1: 基础项目搭建

## 🎯 阶段目标

创建一个能在服务器上直接运行的 Spring Boot 服务，提供基础的健康检查接口。

## 📋 需求清单

### 技术栈
- **项目类型**: Maven
- **语言**: Java 17
- **框架**: Spring Boot 3.3.0
- **包结构**: com.devinsight
- **打包方式**: Jar

### 依赖要求
- ✅ Spring Web
- ✅ Spring Validation
- ✅ Spring Security（基础配置，暂不实现具体逻辑）
- ⛔ 不引入 JPA / MyBatis（后续阶段添加）

## 🏗️ 实现思路

### 1. 项目结构
```
devinsight-backend/
├── pom.xml
├── src/main/
│   ├── java/com/devinsight/
│   │   ├── DevinsightBackendApplication.java
│   │   ├── config/
│   │   │   └── SecurityConfig.java
│   │   └── controller/
│   │       └── HealthController.java
│   └── resources/
│       └── application.yml
```

### 2. 核心配置

#### SecurityConfig
- 禁用 CSRF（方便测试）
- 放行所有请求 `permitAll()`
- 无状态会话管理

#### HealthController
- 提供 `GET /health` 接口
- 返回 JSON: `{"status": "UP", "service": "devinsight-backend"}`

### 3. 应用配置
```yaml
server:
  port: 8080

spring:
  application:
    name: devinsight-backend
```

## ✅ 验收标准

1. 服务能够成功启动
2. 访问 `http://localhost:8080/health` 返回正常响应
3. 无数据库依赖，可独立运行

## 🚀 启动方式

### 使用 IDEA
1. 导入项目到 IntelliJ IDEA
2. 配置 JDK 17
3. 运行 `DevinsightBackendApplication`

### 使用命令行
```bash
cd devinsight-backend
mvn spring-boot:run
```

## 📝 测试验证

```bash
curl http://localhost:8080/health
```

预期响应：
```json
{
  "status": "UP",
  "service": "devinsight-backend"
}
```

---

**完成时间**: 2026-02-01  
**下一阶段**: 用户与项目管理
