package com.devinsight.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devinsight.dto.ProjectCreateRequest;
import com.devinsight.entity.Project;
import com.devinsight.entity.ProjectMember;
import com.devinsight.entity.User;
import com.devinsight.exception.BusinessException;
import com.devinsight.mapper.ProjectMapper;
import com.devinsight.mapper.ProjectMemberMapper;
import com.devinsight.mapper.UserMapper;
import com.devinsight.vo.ProjectVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectService {
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private ProjectMemberMapper projectMemberMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Transactional
    public ProjectVO createProject(Long userId, ProjectCreateRequest request) {
        // 创建项目
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwnerId(userId);
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        
        projectMapper.insert(project);
        
        // 添加创建者为 OWNER
        ProjectMember member = new ProjectMember();
        member.setProjectId(project.getId());
        member.setUserId(userId);
        member.setRole(ProjectMember.ROLE_OWNER);
        member.setJoinedAt(LocalDateTime.now());
        
        projectMemberMapper.insert(member);
        
        return buildProjectVO(project, userId);
    }
    
    public List<ProjectVO> getMyProjects(Long userId) {
        // 查询用户参与的所有项目
        LambdaQueryWrapper<ProjectMember> memberQuery = new LambdaQueryWrapper<>();
        memberQuery.eq(ProjectMember::getUserId, userId);
        List<ProjectMember> members = projectMemberMapper.selectList(memberQuery);
        
        List<ProjectVO> result = new ArrayList<>();
        for (ProjectMember member : members) {
            Project project = projectMapper.selectById(member.getProjectId());
            if (project != null) {
                result.add(buildProjectVO(project, userId));
            }
        }
        
        return result;
    }
    
    public ProjectVO getProjectDetail(Long userId, Long projectId) {
        // 检查权限
        checkProjectAccess(userId, projectId);
        
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(404, "项目不存在");
        }
        
        return buildProjectVO(project, userId);
    }
    
    @Transactional
    public void updateProject(Long userId, Long projectId, ProjectCreateRequest request) {
        // 检查是否为 OWNER
        checkProjectOwner(userId, projectId);
        
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(404, "项目不存在");
        }
        
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setUpdatedAt(LocalDateTime.now());
        
        projectMapper.updateById(project);
    }
    
    @Transactional
    public void deleteProject(Long userId, Long projectId) {
        // 检查是否为 OWNER
        checkProjectOwner(userId, projectId);
        
        // 删除项目（级联删除成员记录）
        projectMapper.deleteById(projectId);
    }
    
    private ProjectVO buildProjectVO(Project project, Long currentUserId) {
        ProjectVO vo = new ProjectVO();
        BeanUtils.copyProperties(project, vo);
        
        // 获取 owner 名称
        User owner = userMapper.selectById(project.getOwnerId());
        if (owner != null) {
            vo.setOwnerName(owner.getUsername());
        }
        
        // 获取当前用户的角色
        LambdaQueryWrapper<ProjectMember> query = new LambdaQueryWrapper<>();
        query.eq(ProjectMember::getProjectId, project.getId())
                .eq(ProjectMember::getUserId, currentUserId);
        ProjectMember member = projectMemberMapper.selectOne(query);
        if (member != null) {
            vo.setMyRole(member.getRole());
        }
        
        return vo;
    }
    
    private void checkProjectAccess(Long userId, Long projectId) {
        LambdaQueryWrapper<ProjectMember> query = new LambdaQueryWrapper<>();
        query.eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId);
        
        if (projectMemberMapper.selectOne(query) == null) {
            throw new BusinessException(403, "无权访问该项目");
        }
    }
    
    private void checkProjectOwner(Long userId, Long projectId) {
        LambdaQueryWrapper<ProjectMember> query = new LambdaQueryWrapper<>();
        query.eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId)
                .eq(ProjectMember::getRole, ProjectMember.ROLE_OWNER);
        
        if (projectMemberMapper.selectOne(query) == null) {
            throw new BusinessException(403, "无权操作该项目，仅限项目所有者");
        }
    }
}
