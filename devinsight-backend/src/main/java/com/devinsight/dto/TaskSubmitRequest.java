package com.devinsight.dto;

import com.devinsight.enums.TaskType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 任务提交请求
 */
@Data
public class TaskSubmitRequest {
    
    /**
     * 项目ID
     */
    @NotNull(message = "项目ID不能为空")
    private Long projectId;
    
    /**
     * 任务类型
     */
    @NotNull(message = "任务类型不能为空")
    private TaskType taskType;
    
    /**
     * 输入数据（JSON格式字符串）
     */
    @NotNull(message = "输入数据不能为空")
    private String inputData;
}
