package com.example.testplatform.report.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;

/**
 * 项目报表行 VO（团队下钻项目列表用）
 */
@ColumnWidth(16)
public class ProjectRowVO {

    /** 项目ID */
    private String projectId;

    /** 项目名称 */
    @ExcelProperty("项目名称")
    private String projectName;

    /** 系统编号 */
    @ExcelProperty("系统编号")
    private String systemCode;

    /** 测试团队 */
    @ExcelProperty("测试团队")
    private String testTeam;

    /** 执行次数 */
    @ExcelProperty("执行次数")
    private long executed;

    /** 覆盖用例数 */
    @ExcelProperty("覆盖用例数")
    private long covered;

    /** 总用例数（当前存量） */
    @ExcelProperty("总用例数")
    private long totalCases;

    /** 执行率（=覆盖/总用例*100，两位小数，总用例为0时null） */
    @ExcelProperty("执行率(%)")
    private Double executionRate;

    /** 成功次数 */
    @ExcelProperty("成功次数")
    private long success;

    /** 失败次数 */
    @ExcelProperty("失败次数")
    private long fail;

    /** 成功率（0~100 两位小数，executed=0 为 null） */
    @ExcelProperty("成功率(%)")
    private Double successRate;

    /** 各执行方式成功率（key=执行方式，动态值域，导出时动态拼列） */
    private java.util.Map<String, Double> typeRates;

    /** 上一周期执行次数（无数据 null） */
    @ExcelProperty("上期执行次数")
    private Long prevExecuted;

    /** 执行次数环比百分比（无数据 null） */
    @ExcelProperty("执行次数环比(%)")
    private Double executedChangeRate;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
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

    public long getExecuted() {
        return executed;
    }

    public void setExecuted(long executed) {
        this.executed = executed;
    }

    public long getCovered() {
        return covered;
    }

    public void setCovered(long covered) {
        this.covered = covered;
    }

    public long getTotalCases() {
        return totalCases;
    }

    public void setTotalCases(long totalCases) {
        this.totalCases = totalCases;
    }

    public Double getExecutionRate() {
        return executionRate;
    }

    public void setExecutionRate(Double executionRate) {
        this.executionRate = executionRate;
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

    public java.util.Map<String, Double> getTypeRates() {
        return typeRates;
    }

    public void setTypeRates(java.util.Map<String, Double> typeRates) {
        this.typeRates = typeRates;
    }

    public Long getPrevExecuted() {
        return prevExecuted;
    }

    public void setPrevExecuted(Long prevExecuted) {
        this.prevExecuted = prevExecuted;
    }

    public Double getExecutedChangeRate() {
        return executedChangeRate;
    }

    public void setExecutedChangeRate(Double executedChangeRate) {
        this.executedChangeRate = executedChangeRate;
    }
}
