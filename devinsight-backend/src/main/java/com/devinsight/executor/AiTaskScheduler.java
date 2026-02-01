package com.devinsight.executor;

import com.devinsight.entity.AiTask;
import com.devinsight.mapper.AiTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 任务调度器
 * 定时扫描 pending 状态的任务并执行
 */
@Slf4j
@Component
@EnableScheduling
public class AiTaskScheduler {
    
    @Autowired
    private AiTaskMapper taskMapper;
    
    /**
     * 定时扫描并执行任务
     * 每5秒执行一次
     */
    @Scheduled(fixedDelay = 5000)
    public void scanAndExecute() {
        // 1. 查询 pending 状态的任务（最多10个）
        List<AiTask> tasks = taskMapper.selectByStatus("pending", 10);
        
        if (tasks.isEmpty()) {
            return;
        }
        
        log.info("扫描到 {} 个待执行任务", tasks.size());
        
        // 2. 逐个处理任务
        for (AiTask task : tasks) {
            try {
                executeTask(task);
            } catch (Exception e) {
                log.error("任务执行异常: taskId={}", task.getId(), e);
            }
        }
    }
    
    /**
     * 执行单个任务
     *
     * @param task 任务对象
     */
    private void executeTask(AiTask task) {
        // 1. 乐观锁：尝试抢占任务
        int rows = taskMapper.updateToRunning(task.getId(), LocalDateTime.now());
        if (rows == 0) {
            log.debug("任务已被其他线程抢占: taskId={}", task.getId());
            return;
        }
        
        log.info("开始执行任务: taskId={}, type={}, projectId={}", 
                task.getId(), task.getTaskType(), task.getProjectId());
        
        try {
            // 2. 执行任务（调用AI）
            String result = processTask(task);
            
            // 3. 更新为成功
            taskMapper.updateToSuccess(task.getId(), result, LocalDateTime.now());
            
            log.info("任务执行成功: taskId={}", task.getId());
            
        } catch (Exception e) {
            // 4. 更新为失败
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = e.getClass().getSimpleName();
            }
            
            taskMapper.updateToFailed(task.getId(), errorMessage, LocalDateTime.now());
            
            log.error("任务执行失败: taskId={}, error={}", task.getId(), errorMessage);
        }
    }
    
    /**
     * 处理任务（调用AI）
     * 当前为模拟实现，后续对接真实 AI API
     *
     * @param task 任务对象
     * @return 处理结果（JSON格式）
     */
    private String processTask(AiTask task) throws Exception {
        // 模拟AI处理耗时
        Thread.sleep(2000);
        
        // 根据任务类型返回模拟结果
        switch (task.getTaskType()) {
            case EXCEPTION_ANALYSIS:
                return generateExceptionAnalysisResult(task.getInputData());
            case LOG_SUMMARY:
                return generateLogSummaryResult(task.getInputData());
            default:
                throw new IllegalArgumentException("未知的任务类型: " + task.getTaskType());
        }
    }
    
    /**
     * 生成异常分析结果（模拟）
     */
    private String generateExceptionAnalysisResult(String inputData) {
        return String.format("""
                {
                  "type": "EXCEPTION_ANALYSIS",
                  "summary": "这是一个空指针异常，通常发生在对象未初始化时访问其属性或方法",
                  "rootCause": "变量未正确初始化",
                  "suggestions": [
                    "检查对象是否正确初始化",
                    "添加空值判断",
                    "使用 Optional 类避免空指针"
                  ],
                  "confidence": 0.85,
                  "analyzedAt": "%s"
                }
                """, LocalDateTime.now());
    }
    
    /**
     * 生成日志总结结果（模拟）
     */
    private String generateLogSummaryResult(String inputData) {
        return String.format("""
                {
                  "type": "LOG_SUMMARY",
                  "summary": "系统运行正常，共处理了1000个请求，平均响应时间200ms",
                  "keyMetrics": {
                    "totalRequests": 1000,
                    "avgResponseTime": "200ms",
                    "errorRate": "0.5%%"
                  },
                  "anomalies": [
                    "14:30-14:35期间响应时间略有上升"
                  ],
                  "recommendations": [
                    "关注数据库查询性能",
                    "考虑增加缓存"
                  ],
                  "analyzedAt": "%s"
                }
                """, LocalDateTime.now());
    }
}
