package com.devinsight.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * AI 任务类型枚举
 */
public enum TaskType {
    
    /**
     * 异常分析
     */
    EXCEPTION_ANALYSIS("EXCEPTION_ANALYSIS", "异常分析"),
    
    /**
     * 日志总结
     */
    LOG_SUMMARY("LOG_SUMMARY", "日志总结");
    
    @EnumValue  // MyBatis-Plus 会使用这个值存储到数据库
    @JsonValue  // JSON 序列化时使用这个值
    private final String value;
    
    private final String description;
    
    TaskType(String value, String description) {
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
