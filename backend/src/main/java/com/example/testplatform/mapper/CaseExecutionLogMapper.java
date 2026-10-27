package com.example.testplatform.mapper;

import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

/**
 * 用例执行日志 Mapper（append-only 执行事实，报表统计来源）
 */
public interface CaseExecutionLogMapper {

    /**
     * 一键执行后，把该计划当前状态（plan_case 最新结果）追加为执行事实（携带执行方式）
     */
    int insertByPlanId(@Param("planId") Long planId,
                       @Param("executeTime") Long executeTime,
                       @Param("execDate") LocalDate execDate,
                       @Param("execType") String execType);

    /**
     * 计划时间变更：同步该计划全部执行事实的执行时间与执行日期（分区归位）
     */
    int updateTimeByPlanId(@Param("planId") Long planId,
                           @Param("executeTime") Long executeTime,
                           @Param("execDate") LocalDate execDate);

    /**
     * 删除计划的全部执行事实（计划删除时调用）
     */
    int deleteByPlanId(@Param("planId") Long planId);

    /**
     * 删除项目的全部执行事实（项目删除时调用）
     */
    int deleteByProjectId(@Param("projectId") String projectId);

    /**
     * 删除某用例的全部执行事实（用例删除时调用，避免统计复活）
     */
    int deleteByCaseId(@Param("caseId") Long caseId);
}
