package com.example.testplatform.report.vo;

/**
 * 趋势图数据点 VO
 */
public class TrendPointVO {

    /** 横轴标签（日粒度 yyyy-MM-dd；周粒度为该周起始日） */
    private String date;

    /** 执行次数 */
    private long executed;

    /** 成功次数 */
    private long success;

    /** 失败次数 */
    private long fail;

    /** 成功率（0~100，两位小数；executed=0 时为 null） */
    private Double successRate;

    public TrendPointVO() {
    }

    public TrendPointVO(String date, long executed, long success, long fail, Double successRate) {
        this.date = date;
        this.executed = executed;
        this.success = success;
        this.fail = fail;
        this.successRate = successRate;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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
