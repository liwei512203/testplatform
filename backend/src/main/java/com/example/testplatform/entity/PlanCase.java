package com.example.testplatform.entity;

import java.time.LocalDate;

/**
 * 版本计划关联用例
 */
public class PlanCase {

    /** 执行结果常量 */
    public static final String RESULT_PENDING = "未执行";
    public static final String RESULT_SUCCESS = "成功";
    public static final String RESULT_FAIL = "失败";

    /** 主键 */
    private Long id;

    /** 版本计划ID */
    private Long planId;

    /** 计划日期（版本计划创建时间对应日期，分区键） */
    private LocalDate planDate;

    /** 测试用例ID */
    private Long caseId;

    /** 执行结果：未执行/成功/失败 */
    private String result;

    /** 最近一次执行方式 */
    private String execType;

    /** 执行时间（与版本计划创建时间保持一致） */
    private Long executeTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public LocalDate getPlanDate() {
        return planDate;
    }

    public void setPlanDate(LocalDate planDate) {
        this.planDate = planDate;
    }

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getExecType() {
        return execType;
    }

    public void setExecType(String execType) {
        this.execType = execType;
    }

    public Long getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(Long executeTime) {
        this.executeTime = executeTime;
    }
}

