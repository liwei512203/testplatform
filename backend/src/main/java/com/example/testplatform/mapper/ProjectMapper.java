package com.example.testplatform.mapper;

import com.example.testplatform.entity.Project;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目信息 Mapper
 */
public interface ProjectMapper {

    /**
     * 查询全部项目
     */
    List<Project> selectAll(@Param("keyword") String keyword, @Param("testTeam") String testTeam);

    /**
     * 按ID查询
     */
    Project selectById(@Param("id") String id);

    /**
     * 新增
     */
    int insert(Project project);

    /**
     * 更新
     */
    int update(Project project);

    /**
     * 删除
     */
    int deleteById(@Param("id") String id);
}
