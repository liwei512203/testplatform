package com.example.testplatform.vo;

import com.example.testplatform.entity.PlanCase;

/**
 * 版本计划用例视图对象（附带用例名称与内容）
 */
public class PlanCaseVO extends PlanCase {

    /** 用例名称 */
    private String caseName;

    /** 用例内容（JSON） */
    private String content;

    public String getCaseName() {
        return caseName;
    }

    public void setCaseName(String caseName) {
        this.caseName = caseName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
