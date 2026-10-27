package com.example.testplatform.report.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 报表查询 Mapper（只读两张日统计表与项目/用例表）
 */
public interface ReportMapper {

    /**
     * 聚合统计（卡片用）：项目日表按条件求和。
     * 返回 executed/success/fail 三个键
     */
    Map<String, Object> sumDaily(@Param("projectId") String projectId,
                                 @Param("testTeam") String testTeam,
                                 @Param("startDate") String startDate,
                                 @Param("endDate") String endDate);

    /**
     * 覆盖用例数：用例日表按条件去重计数
     */
    long countCoveredCases(@Param("projectId") String projectId,
                           @Param("testTeam") String testTeam,
                           @Param("startDate") String startDate,
                           @Param("endDate") String endDate);

    /**
     * 总用例数（当前存量用例，不受时间过滤）：可选项目/团队过滤
     */
    long countTotalCases(@Param("projectId") String projectId,
                         @Param("testTeam") String testTeam);

    /**
     * 按团队统计总用例数
     */
    List<Map<String, Object>> totalCasesByTeam();

    /**
     * 按项目统计总用例数（可按团队过滤）
     */
    List<Map<String, Object>> totalCasesByProject(@Param("testTeam") String testTeam);

    /**
     * 趋势（日粒度）：项目日表按天分组
     */
    List<Map<String, Object>> trendByDay(@Param("projectId") String projectId,
                                         @Param("testTeam") String testTeam,
                                         @Param("startDate") String startDate,
                                         @Param("endDate") String endDate);

    /**
     * 趋势（周粒度）：按周起始日分组
     */
    List<Map<String, Object>> trendByWeek(@Param("projectId") String projectId,
                                          @Param("testTeam") String testTeam,
                                          @Param("startDate") String startDate,
                                          @Param("endDate") String endDate);

    /**
     * 团队维度聚合：项目日表按团队分组（含上一周期对照值）
     */
    List<Map<String, Object>> sumByTeam(@Param("startDate") String startDate,
                                        @Param("endDate") String endDate,
                                        @Param("prevStartDate") String prevStartDate,
                                        @Param("prevEndDate") String prevEndDate);

    /**
     * 团队覆盖用例数：用例日表按团队分组去重计数（当前周期）
     */
    List<Map<String, Object>> coveredByTeam(@Param("startDate") String startDate,
                                            @Param("endDate") String endDate);

    /**
     * 项目维度聚合（团队下钻）：统计表 join 项目表，outer 补无执行项目
     */
    List<Map<String, Object>> projectRows(@Param("testTeam") String testTeam,
                                          @Param("startDate") String startDate,
                                          @Param("endDate") String endDate,
                                          @Param("prevStartDate") String prevStartDate,
                                          @Param("prevEndDate") String prevEndDate);

    /**
     * 项目覆盖用例数：用例日表按项目分组去重计数
     */
    List<Map<String, Object>> coveredByProject(@Param("testTeam") String testTeam,
                                               @Param("startDate") String startDate,
                                               @Param("endDate") String endDate);

    /**
     * 用例明细分页查询
     */
    List<Map<String, Object>> caseRows(@Param("projectId") String projectId,
                                       @Param("startDate") String startDate,
                                       @Param("endDate") String endDate,
                                       @Param("keyword") String keyword,
                                       @Param("orderClause") String orderClause,
                                       @Param("offset") Integer offset,
                                       @Param("limit") Integer limit);

    /**
     * 用例明细计数
     */
    long countCaseRows(@Param("projectId") String projectId,
                       @Param("startDate") String startDate,
                       @Param("endDate") String endDate,
                       @Param("keyword") String keyword);

    /**
     * 失败TOP用例
     */
    List<Map<String, Object>> topFailures(@Param("projectId") String projectId,
                                          @Param("startDate") String startDate,
                                          @Param("endDate") String endDate,
                                          @Param("limit") Integer limit);

    /**
     * 按执行方式聚合（总览卡片下方的分方式统计）：
     * 可选项目/团队过滤，返回 execType/executed/success/fail
     */
    List<Map<String, Object>> sumByExecType(@Param("projectId") String projectId,
                                            @Param("testTeam") String testTeam,
                                            @Param("startDate") String startDate,
                                            @Param("endDate") String endDate);

    /**
     * 团队 x 执行方式聚合：返回 testTeam/execType/executed/success/fail
     */
    List<Map<String, Object>> sumByTeamAndType(@Param("startDate") String startDate,
                                               @Param("endDate") String endDate);

    /**
     * 项目 x 执行方式聚合（可按团队过滤）：返回 projectId/execType/executed/success/fail
     */
    List<Map<String, Object>> sumByProjectAndType(@Param("testTeam") String testTeam,
                                                  @Param("startDate") String startDate,
                                                  @Param("endDate") String endDate);

    /**
     * 项目下用例 x 执行方式聚合（用例明细分方式成功率）：返回 caseId/execType/executed/success/fail
     */
    List<Map<String, Object>> sumCaseByProjectAndType(@Param("projectId") String projectId,
                                                      @Param("startDate") String startDate,
                                                      @Param("endDate") String endDate);
}
