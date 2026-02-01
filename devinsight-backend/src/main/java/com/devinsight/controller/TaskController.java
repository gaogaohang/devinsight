package com.devinsight.controller;

import com.devinsight.dto.TaskSubmitRequest;
import com.devinsight.service.TaskService;
import com.devinsight.vo.TaskVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 任务控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    
    @Autowired
    private TaskService taskService;
    
    /**
     * 提交任务
     *
     * @param request     提交请求
     * @param httpRequest HTTP请求
     * @return 任务ID
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> submitTask(@Valid @RequestBody TaskSubmitRequest request,
                                                            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Long taskId = taskService.submitTask(request, userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "任务提交成功");
        result.put("data", taskId);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 查询任务详情
     *
     * @param id          任务ID
     * @param httpRequest HTTP请求
     * @return 任务详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTask(@PathVariable Long id,
                                                         HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        TaskVO task = taskService.getTask(id, userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", task);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 查询项目下的任务列表
     *
     * @param projectId   项目ID
     * @param httpRequest HTTP请求
     * @return 任务列表
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<Map<String, Object>> getProjectTasks(@PathVariable Long projectId,
                                                                 HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        List<TaskVO> tasks = taskService.getProjectTasks(projectId, userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", tasks);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 查询我的任务列表
     *
     * @param httpRequest HTTP请求
     * @return 任务列表
     */
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyTasks(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        List<TaskVO> tasks = taskService.getUserTasks(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", tasks);
        return ResponseEntity.ok(result);
    }
}
