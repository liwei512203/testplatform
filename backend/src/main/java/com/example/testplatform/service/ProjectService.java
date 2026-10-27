package com.example.testplatform.service;

import com.example.testplatform.entity.Project;

import java.util.List;

/**
 * 项目信息服务
 */
public interface ProjectService {

    /**
     * 项目列表（可按名称/系统编号模糊查询，可按测试团队过滤）
     */
    List<Project> list(String keyword, String testTeam);

    /**
     * 项目详情
     */
    Project getById(String id);

    /**
     * 新增项目
     */
    Project create(Project project);

    /**
     * 更新项目
     */
    void update(Project project);

    /**
     * 删除项目（同时删除该项目下的用例）
     */
    void delete(String id);
}
