package com.devinsight.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * AI 任务状态枚举
 */
public enum TaskStatus {
    
    /**
     * 待执行
     */
    PENDING("pending", "待执行"),
    
    /**
     * 执行中
     */
    RUNNING("running", "执行中"),
    
    /**
     * 执行成功
     */
    SUCCESS("success", "执行成功"),
    
    /**
     * 执行失败
     */
    FAILED("failed", "执行失败");
    
    @EnumValue  // MyBatis-Plus 会使用这个值存储到数据库
    @JsonValue  // JSON 序列化时使用这个值
    private final String value;
    
    private final String description;
    
    TaskStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
}
