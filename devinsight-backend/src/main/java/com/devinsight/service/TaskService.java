package com.devinsight.service;

import com.devinsight.dto.TaskSubmitRequest;
import com.devinsight.entity.AiTask;
import com.devinsight.entity.ProjectMember;
import com.devinsight.enums.TaskStatus;
import com.devinsight.exception.BusinessException;
import com.devinsight.mapper.AiTaskMapper;
import com.devinsight.mapper.ProjectMemberMapper;
import com.devinsight.vo.TaskVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务服务
 */
@Slf4j
@Service
public class TaskService {
    
    @Autowired
    private AiTaskMapper taskMapper;
    
    @Autowired
    private ProjectMemberMapper projectMemberMapper;
    
    /**
     * 提交任务
     *
     * @param request 提交请求
     * @param userId  当前用户ID
     * @return 任务ID
     */
    public Long submitTask(TaskSubmitRequest request, Long userId) {
        // 1. 检查用户是否是项目成员
        ProjectMember member = projectMemberMapper.selectByProjectAndUser(request.getProjectId(), userId);
        if (member == null) {
            throw new BusinessException("无权限访问该项目");
        }
        
        // 2. 创建任务
        AiTask task = new AiTask();
        task.setProjectId(request.getProjectId());
        task.setUserId(userId);
        task.setTaskType(request.getTaskType());
        task.setStatus(TaskStatus.PENDING);
        task.setInputData(request.getInputData());
        task.setCreatedAt(LocalDateTime.now());
        
        taskMapper.insert(task);
        
        log.info("用户 {} 提交任务: taskId={}, type={}, projectId={}", 
                userId, task.getId(), task.getTaskType(), task.getProjectId());
        
        return task.getId();
    }
    
    /**
     * 查询任务详情
     *
     * @param taskId 任务ID
     * @param userId 当前用户ID
     * @return 任务详情
     */
    public TaskVO getTask(Long taskId, Long userId) {
        AiTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        
        // 检查权限：必须是项目成员
        ProjectMember member = projectMemberMapper.selectByProjectAndUser(task.getProjectId(), userId);
        if (member == null) {
            throw new BusinessException("无权限访问该任务");
        }
        
        return convertToVO(task);
    }
    
    /**
     * 查询项目下的任务列表
     *
     * @param projectId 项目ID
     * @param userId    当前用户ID
     * @return 任务列表
     */
    public List<TaskVO> getProjectTasks(Long projectId, Long userId) {
        // 检查权限
        ProjectMember member = projectMemberMapper.selectByProjectAndUser(projectId, userId);
        if (member == null) {
            throw new BusinessException("无权限访问该项目");
        }
        
        List<AiTask> tasks = taskMapper.selectByProjectId(projectId);
        return tasks.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 查询用户的任务列表
     *
     * @param userId 用户ID
     * @return 任务列表
     */
    public List<TaskVO> getUserTasks(Long userId) {
        List<AiTask> tasks = taskMapper.selectByUserId(userId);
        return tasks.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 转换为VO对象
     */
    private TaskVO convertToVO(AiTask task) {
        TaskVO vo = new TaskVO();
        BeanUtils.copyProperties(task, vo);
        vo.setTaskTypeDesc(task.getTaskType().getDescription());
        vo.setStatusDesc(task.getStatus().getDescription());
        return vo;
    }
}
