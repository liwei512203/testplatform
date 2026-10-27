package com.example.testplatform.entity;

import com.example.testplatform.enums.TestTeam;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 项目信息
 */
public class Project {

    /** 主键UUID */
    private String id;

    /** 项目名称 */
    @NotBlank(message = "项目名称不能为空")
    private String name;

    /** 系统编号 */
    @NotBlank(message = "系统编号不能为空")
    private String systemCode;

    /** 所属测试团队（企金测试团队/零售测试团队/数据测试团队/公共测试团队） */
    @NotBlank(message = "测试团队不能为空")
    @Pattern(
            regexp = TestTeam.REGEX,
            message = "测试团队取值不合法，仅支持：" + TestTeam.TIP
    )
    private String testTeam;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSystemCode() {
        return systemCode;
    }

    public void setSystemCode(String systemCode) {
        this.systemCode = systemCode;
    }

    public String getTestTeam() {
        return testTeam;
    }

    public void setTestTeam(String testTeam) {
        this.testTeam = testTeam;
    }
}
