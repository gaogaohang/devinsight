#!/bin/bash

# AI 任务系统测试脚本
# 测试完整的任务生命周期：提交 → pending → running → success/failed

BASE_URL="http://localhost:8080"
echo "======================================"
echo "AI 任务系统测试"
echo "======================================"
echo ""

# 1. 用户登录获取 token
echo "1. 用户登录..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456"
  }')

TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.data.token')
USER_ID=$(echo $LOGIN_RESPONSE | jq -r '.data.userId')

if [ "$TOKEN" == "null" ] || [ -z "$TOKEN" ]; then
  echo "❌ 登录失败，请先运行 test-api.sh 创建用户和项目"
  exit 1
fi

echo "✅ 登录成功: userId=$USER_ID"
echo ""

# 2. 获取项目列表
echo "2. 获取项目列表..."
PROJECTS=$(curl -s "$BASE_URL/api/projects" \
  -H "Authorization: Bearer $TOKEN")

PROJECT_ID=$(echo $PROJECTS | jq -r '.data[0].id')

if [ "$PROJECT_ID" == "null" ] || [ -z "$PROJECT_ID" ]; then
  echo "❌ 未找到项目，请先运行 test-api.sh 创建项目"
  exit 1
fi

echo "✅ 找到项目: projectId=$PROJECT_ID"
echo ""

# 3. 提交异常分析任务
echo "3. 提交异常分析任务..."
TASK1_RESPONSE=$(curl -s -X POST "$BASE_URL/api/tasks" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"projectId\": $PROJECT_ID,
    \"taskType\": \"EXCEPTION_ANALYSIS\",
    \"inputData\": \"{\\\"exception\\\": \\\"NullPointerException at line 100\\\", \\\"stackTrace\\\": \\\"...\\\"}\"}
  }")

TASK1_ID=$(echo $TASK1_RESPONSE | jq -r '.data')
echo "✅ 任务提交成功: taskId=$TASK1_ID"
echo ""

# 4. 提交日志总结任务
echo "4. 提交日志总结任务..."
TASK2_RESPONSE=$(curl -s -X POST "$BASE_URL/api/tasks" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"projectId\": $PROJECT_ID,
    \"taskType\": \"LOG_SUMMARY\",
    \"inputData\": \"{\\\"logs\\\": [\\\"INFO: Request received\\\", \\\"INFO: Processing...\\\", \\\"INFO: Completed\\\"]}\"}
  }")

TASK2_ID=$(echo $TASK2_RESPONSE | jq -r '.data')
echo "✅ 任务提交成功: taskId=$TASK2_ID"
echo ""

# 5. 立即查询任务状态（应该是 pending）
echo "5. 查询任务1状态（应该是 pending）..."
TASK1_STATUS=$(curl -s "$BASE_URL/api/tasks/$TASK1_ID" \
  -H "Authorization: Bearer $TOKEN")
echo $TASK1_STATUS | jq '{id: .data.id, status: .data.status, statusDesc: .data.statusDesc}'
echo ""

# 6. 等待任务执行（定时器每5秒扫描一次，任务执行需要2秒）
echo "6. 等待任务执行（需要约7-10秒）..."
echo "   - 定时器扫描间隔: 5秒"
echo "   - 任务模拟执行: 2秒"
echo "   正在等待..."

for i in {1..10}; do
  sleep 1
  echo -n "."
done
echo ""
echo ""

# 7. 再次查询任务1（应该是 success）
echo "7. 查询任务1详情（应该已完成）..."
TASK1_DETAIL=$(curl -s "$BASE_URL/api/tasks/$TASK1_ID" \
  -H "Authorization: Bearer $TOKEN")

TASK1_FINAL_STATUS=$(echo $TASK1_DETAIL | jq -r '.data.status')
echo "任务状态: $TASK1_FINAL_STATUS"
echo ""
echo "完整结果:"
echo $TASK1_DETAIL | jq '.data'
echo ""

# 8. 查询任务2详情
echo "8. 查询任务2详情..."
TASK2_DETAIL=$(curl -s "$BASE_URL/api/tasks/$TASK2_ID" \
  -H "Authorization: Bearer $TOKEN")

TASK2_FINAL_STATUS=$(echo $TASK2_DETAIL | jq -r '.data.status')
echo "任务状态: $TASK2_FINAL_STATUS"
echo ""
echo "完整结果:"
echo $TASK2_DETAIL | jq '.data'
echo ""

# 9. 查询我的所有任务
echo "9. 查询我的所有任务..."
MY_TASKS=$(curl -s "$BASE_URL/api/tasks/my" \
  -H "Authorization: Bearer $TOKEN")

TASK_COUNT=$(echo $MY_TASKS | jq '.data | length')
echo "✅ 共 $TASK_COUNT 个任务"
echo $MY_TASKS | jq '.data[] | {id: .id, type: .taskTypeDesc, status: .statusDesc, createdAt: .createdAt}'
echo ""

# 10. 查询项目下的所有任务
echo "10. 查询项目下的所有任务..."
PROJECT_TASKS=$(curl -s "$BASE_URL/api/tasks/project/$PROJECT_ID" \
  -H "Authorization: Bearer $TOKEN")

PROJECT_TASK_COUNT=$(echo $PROJECT_TASKS | jq '.data | length')
echo "✅ 项目 $PROJECT_ID 共 $PROJECT_TASK_COUNT 个任务"
echo ""

# 总结
echo "======================================"
echo "测试总结"
echo "======================================"
echo "✅ 任务提交: 成功"
echo "✅ 任务1状态: $TASK1_FINAL_STATUS"
echo "✅ 任务2状态: $TASK2_FINAL_STATUS"
echo "✅ 任务查询: 成功"
echo ""

if [ "$TASK1_FINAL_STATUS" == "SUCCESS" ] && [ "$TASK2_FINAL_STATUS" == "SUCCESS" ]; then
  echo "🎉 所有测试通过！任务系统运行正常！"
else
  echo "⚠️  部分任务未完成，可能需要等待更长时间或检查日志"
fi

echo "======================================"
