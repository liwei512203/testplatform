package com.example.testplatform.report.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;

import java.util.Map;

/**
 * 用例执行明细行 VO
 */
@ColumnWidth(20)
public class CaseRowVO {

    /** 用例ID */
    @ExcelProperty("用例ID")
    private Long caseId;

    /** 用例名称 */
    @ExcelProperty("用例名称")
    private String caseName;

    /** 执行次数 */
    @ExcelProperty("执行次数")
    private long executed;

    /** 成功次数 */
    @ExcelProperty("成功次数")
    private long success;

    /** 失败次数 */
    @ExcelProperty("失败次数")
    private long fail;

    /** 各执行方式成功率（key=执行方式，动态值域；无数据为空Map） */
    private Map<String, Double> typeRates;

    /** 成功率（0~100 两位小数，executed=0 为 null） */
    @ExcelProperty("成功率(%)")
    private Double successRate;

    /** 最近执行日期（yyyy-MM-dd，无执行 null） */
    @ExcelProperty("最近执行日期")
    private String lastExecDate;

    public Map<String, Double> getTypeRates() {
        return typeRates;
    }

    public void setTypeRates(Map<String, Double> typeRates) {
        this.typeRates = typeRates;
    }

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public String getCaseName() {
        return caseName;
    }

    public void setCaseName(String caseName) {
        this.caseName = caseName;
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

    public String getLastExecDate() {
        return lastExecDate;
    }

    public void setLastExecDate(String lastExecDate) {
        this.lastExecDate = lastExecDate;
    }
}
