package com.devinsight.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devinsight.entity.ProjectMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProjectMemberMapper extends BaseMapper<ProjectMember> {
    
    /**
     * 查询用户在项目中的成员记录
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @return 成员记录，不存在返回null
     */
    @Select("SELECT * FROM project_member WHERE project_id = #{projectId} AND user_id = #{userId}")
    ProjectMember selectByProjectAndUser(@Param("projectId") Long projectId, @Param("userId") Long userId);
}
