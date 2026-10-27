package com.example.testplatform.vo;

import com.example.testplatform.entity.VersionPlan;

/**
 * 版本计划视图对象（附带执行统计）
 */
public class VersionPlanVO extends VersionPlan {

    /** 关联用例总数 */
    private long totalCases;

    /** 执行成功数 */
    private long successCount;

    /** 执行失败数 */
    private long failCount;

    /** 未执行数 */
    private long unexecutedCount;

    public long getTotalCases() {
        return totalCases;
    }

    public void setTotalCases(long totalCases) {
        this.totalCases = totalCases;
    }

    public long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(long successCount) {
        this.successCount = successCount;
    }

    public long getFailCount() {
        return failCount;
    }

    public void setFailCount(long failCount) {
        this.failCount = failCount;
    }

    public long getUnexecutedCount() {
        return unexecutedCount;
    }

    public void setUnexecutedCount(long unexecutedCount) {
        this.unexecutedCount = unexecutedCount;
    }
}
