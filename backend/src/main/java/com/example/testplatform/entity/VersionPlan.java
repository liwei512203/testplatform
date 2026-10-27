package com.example.testplatform.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 版本计划
 */
public class VersionPlan {

    /** 版本计划ID */
    private Long id;

    /** 所属项目ID */
    @NotBlank(message = "所属项目ID不能为空")
    private String projectId;

    /** 版本名称（迭代轮次，如 V1.0） */
    @NotBlank(message = "版本名称不能为空")
    private String name;

    /** 计划创建时间（用户指定，毫秒时间戳） */
    @NotNull(message = "计划时间不能为空")
    private Long createTime;

    /** 记录更新时间 */
    private Long updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
}
