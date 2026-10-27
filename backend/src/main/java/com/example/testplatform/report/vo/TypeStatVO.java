package com.example.testplatform.report.vo;

/**
 * 按执行方式统计 VO（系统/项目总览与团队/项目列表通用）
 */
public class TypeStatVO {

    /** 执行方式：手动执行/定时任务执行/流水线触发执行 */
    private String execType;

    /** 执行次数 */
    private long executed;

    /** 成功次数 */
    private long success;

    /** 失败次数 */
    private long fail;

    /** 成功率（0~100 两位小数，executed=0 为 null） */
    private Double successRate;

    public TypeStatVO() {
    }

    public TypeStatVO(String execType, long executed, long success, long fail, Double successRate) {
        this.execType = execType;
        this.executed = executed;
        this.success = success;
        this.fail = fail;
        this.successRate = successRate;
    }

    public String getExecType() {
        return execType;
    }

    public void setExecType(String execType) {
        this.execType = execType;
    }

    public long getExecuted() {
        return executed;
    }

    public void setExecuted(long executed) {
        this.executed = executed;
    }

    public long getSuccess() {
        return success;
    }

    public void setSuccess(long success) {
        this.success = success;
    }

    public long getFail() {
        return fail;
    }

    public void setFail(long fail) {
        this.fail = fail;
    }

    public Double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(Double successRate) {
        this.successRate = successRate;
    }
}
