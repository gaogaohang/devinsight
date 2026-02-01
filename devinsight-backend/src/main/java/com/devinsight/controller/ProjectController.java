package com.devinsight.controller;

import com.devinsight.dto.ProjectCreateRequest;
import com.devinsight.service.ProjectService;
import com.devinsight.vo.ProjectVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    
    @Autowired
    private ProjectService projectService;
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createProject(
            HttpServletRequest request,
            @Valid @RequestBody ProjectCreateRequest projectRequest) {
        Long userId = (Long) request.getAttribute("userId");
        ProjectVO project = projectService.createProject(userId, projectRequest);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "创建成功");
        result.put("data", project);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMyProjects(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<ProjectVO> projects = projectService.getMyProjects(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", projects);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProjectDetail(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        ProjectVO project = projectService.getProjectDetail(userId, id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", project);
        return ResponseEntity.ok(result);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateProject(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody ProjectCreateRequest projectRequest) {
        Long userId = (Long) request.getAttribute("userId");
        projectService.updateProject(userId, id, projectRequest);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "更新成功");
        return ResponseEntity.ok(result);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteProject(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        projectService.deleteProject(userId, id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "删除成功");
        return ResponseEntity.ok(result);
    }
}
