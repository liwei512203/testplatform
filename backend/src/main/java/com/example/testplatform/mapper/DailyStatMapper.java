package com.example.testplatform.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 用例执行每日统计 Mapper（报表数据源维护）
 */
public interface DailyStatMapper {

    /**
     * 删除某项目某天的统计行（重算前先清理）
     */
    int deleteByProjectAndDate(@Param("projectId") String projectId, @Param("statDate") String statDate);

    /**
     * 按项目+日期从计划用例明细重算并插入统计行（仅统计已执行用例）
     */
    int insertAggregateByDate(@Param("projectId") String projectId, @Param("statDate") String statDate);

    /**
     * 项目所属测试团队变更时同步两张统计表
     */
    int updateTeamByProject(@Param("projectId") String projectId, @Param("testTeam") String testTeam);

    /**
     * 删除某项目全部统计行（项目删除时调用，项目日表）
     */
    int deleteByProject(@Param("projectId") String projectId);

    /**
     * 项目所属测试团队变更时同步用例日表
     */
    int updateCaseTeamByProject(@Param("projectId") String projectId, @Param("testTeam") String testTeam);

    /**
     * 删除某项目某天用例日统计行（重算前先清理）
     */
    int deleteCaseByProjectAndDate(@Param("projectId") String projectId, @Param("statDate") String statDate);

    /**
     * 按项目+日期从计划用例明细重算并插入用例日统计（按 天×项目×用例 聚合）
     */
    int insertCaseAggregateByDate(@Param("projectId") String projectId, @Param("statDate") String statDate);

    /**
     * 删除某项目全部用例日统计行（项目删除时调用）
     */
    int deleteCaseByProject(@Param("projectId") String projectId);

    /**
     * 删除某测试用例全部用例日统计行（用例删除时调用）
     */
    int deleteCaseStatByCaseId(@Param("caseId") Long caseId);

    /**
     * 跑批：删除某日期项目日表全部统计行
     */
    int deleteStatByDate(@Param("statDate") String statDate);

    /**
     * 跑批：删除某日期用例日表全部统计行
     */
    int deleteCaseStatByDate(@Param("statDate") String statDate);

    /**
     * 跑批：按日期从执行日志重算全部项目的项目日统计
     */
    int insertStatByDate(@Param("statDate") String statDate);

    /**
     * 跑批：按日期从执行日志重算全部项目的用例日统计
     */
    int insertCaseStatByDate(@Param("statDate") String statDate);

    /**
     * 全量重算：日志表中实际存在数据的最早/最晚执行日期（无数据返回 null）
     */
    Map<String, Object> selectExecDateRange();
}
