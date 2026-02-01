package com.devinsight.vo;

import com.devinsight.enums.TaskStatus;
import com.devinsight.enums.TaskType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务响应对象
 */
@Data
public class TaskVO {
    
    /**
     * 任务ID
     */
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
     * 任务类型描述
     */
    private String taskTypeDesc;
    
    /**
     * 任务状态
     */
    private TaskStatus status;
    
    /**
     * 任务状态描述
     */
    private String statusDesc;
    
    /**
     * 输入数据
     */
    private String inputData;
    
    /**
     * 结果数据
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
