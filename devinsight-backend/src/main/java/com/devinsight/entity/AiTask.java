package com.devinsight.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devinsight.enums.TaskStatus;
import com.devinsight.enums.TaskType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 任务实体
 */
@Data
@TableName("ai_task")
public class AiTask {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 项目ID
     */
    private Long projectId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 任务类型
     */
    private TaskType taskType;
    
    /**
     * 任务状态
     */
    private TaskStatus status;
    
    /**
     * 输入数据（JSON格式）
     */
    private String inputData;
    
    /**
     * 结果数据（JSON格式）
     */
    private String resultData;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 开始执行时间
     */
    private LocalDateTime startedAt;
    
    /**
     * 完成时间
     */
    private LocalDateTime completedAt;
}
