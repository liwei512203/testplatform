package com.example.testplatform.entity;

import javax.validation.constraints.NotBlank;

/**
 * 测试用例信息
 */
public class TestCase {

    /** 用例ID */
    private Long id;

    /** 所属项目ID */
    @NotBlank(message = "所属项目ID不能为空")
    private String projectId;

    /** 用例名称 */
    @NotBlank(message = "用例名称不能为空")
    private String name;

    /** 用例内容（JSON 字符串：前置条件/步骤/预期结果等） */
    private String content;

    /** 创建时间（毫秒时间戳） */
    private Long createTime;

    /** 更新时间（毫秒时间戳） */
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
