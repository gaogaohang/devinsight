package com.devinsight.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devinsight.entity.AiTask;
import com.devinsight.enums.TaskStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 任务 Mapper
 */
@Mapper
public interface AiTaskMapper extends BaseMapper<AiTask> {
    
    /**
     * 查询指定状态的任务（用于定时扫描）
     *
     * @param status 任务状态
     * @param limit  限制数量
     * @return 任务列表
     */
    @Select("SELECT * FROM ai_task WHERE status = #{status} ORDER BY created_at ASC LIMIT #{limit}")
    List<AiTask> selectByStatus(@Param("status") String status, @Param("limit") int limit);
    
    /**
     * 乐观锁：尝试将任务状态从 pending 更新为 running
     *
     * @param taskId 任务ID
     * @return 影响行数（1表示成功，0表示失败）
     */
    @Update("UPDATE ai_task SET status = 'running', started_at = #{now} " +
            "WHERE id = #{taskId} AND status = 'pending'")
    int updateToRunning(@Param("taskId") Long taskId, @Param("now") LocalDateTime now);
    
    /**
     * 更新任务为成功状态
     *
     * @param taskId     任务ID
     * @param resultData 结果数据
     * @param now        完成时间
     * @return 影响行数
     */
    @Update("UPDATE ai_task SET status = 'success', result_data = #{resultData}, completed_at = #{now} " +
            "WHERE id = #{taskId}")
    int updateToSuccess(@Param("taskId") Long taskId,
                        @Param("resultData") String resultData,
                        @Param("now") LocalDateTime now);
    
    /**
     * 更新任务为失败状态
     *
     * @param taskId       任务ID
     * @param errorMessage 错误信息
     * @param now          完成时间
     * @return 影响行数
     */
    @Update("UPDATE ai_task SET status = 'failed', error_message = #{errorMessage}, completed_at = #{now} " +
            "WHERE id = #{taskId}")
    int updateToFailed(@Param("taskId") Long taskId,
                       @Param("errorMessage") String errorMessage,
                       @Param("now") LocalDateTime now);
    
    /**
     * 查询项目下的任务列表
     *
     * @param projectId 项目ID
     * @return 任务列表
     */
    @Select("SELECT * FROM ai_task WHERE project_id = #{projectId} ORDER BY created_at DESC")
    List<AiTask> selectByProjectId(@Param("projectId") Long projectId);
    
    /**
     * 查询用户的任务列表
     *
     * @param userId 用户ID
     * @return 任务列表
     */
    @Select("SELECT * FROM ai_task WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<AiTask> selectByUserId(@Param("userId") Long userId);
}
