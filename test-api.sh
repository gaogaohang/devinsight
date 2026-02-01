#!/bin/bash

BASE_URL="http://localhost:8080"

echo "=========================================="
echo "DevInsight API 测试脚本"
echo "=========================================="
echo ""

# 1. 测试健康检查
echo "1. 测试健康检查接口"
curl -s "$BASE_URL/health" | jq .
echo -e "\n"

# 2. 注册用户
echo "2. 注册新用户"
curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456",
    "email": "test@example.com"
  }' | jq .
echo -e "\n"

# 3. 登录获取 token
echo "3. 用户登录"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456"
  }')

echo "$LOGIN_RESPONSE" | jq .

TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.token')
echo "获取到的 Token: $TOKEN"
echo -e "\n"

# 4. 获取当前用户信息
echo "4. 获取当前用户信息"
curl -s "$BASE_URL/api/users/me" \
  -H "Authorization: Bearer $TOKEN" | jq .
echo -e "\n"

# 5. 创建项目
echo "5. 创建项目"
PROJECT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/projects" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "AI异常分析",
    "description": "生产环境日志智能分析项目"
  }')

echo "$PROJECT_RESPONSE" | jq .

PROJECT_ID=$(echo "$PROJECT_RESPONSE" | jq -r '.data.id')
echo "创建的项目 ID: $PROJECT_ID"
echo -e "\n"

# 6. 获取我的项目列表
echo "6. 获取我的项目列表"
curl -s "$BASE_URL/api/projects" \
  -H "Authorization: Bearer $TOKEN" | jq .
echo -e "\n"

# 7. 获取项目详情
echo "7. 获取项目详情"
curl -s "$BASE_URL/api/projects/$PROJECT_ID" \
  -H "Authorization: Bearer $TOKEN" | jq .
echo -e "\n"

# 8. 更新项目
echo "8. 更新项目信息"
curl -s -X PUT "$BASE_URL/api/projects/$PROJECT_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "AI异常分析（已更新）",
    "description": "生产环境日志智能分析项目 - 更新版"
  }' | jq .
echo -e "\n"

# 9. 再次查看项目列表
echo "9. 再次查看项目列表（验证更新）"
curl -s "$BASE_URL/api/projects" \
  -H "Authorization: Bearer $TOKEN" | jq .
echo -e "\n"

echo "=========================================="
echo "测试完成！"
echo "=========================================="
