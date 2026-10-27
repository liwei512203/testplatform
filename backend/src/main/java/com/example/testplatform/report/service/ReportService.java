package com.example.testplatform.report.service;

import com.example.testplatform.common.PageResult;
import com.example.testplatform.report.vo.CaseRowVO;
import com.example.testplatform.report.vo.OverviewVO;
import com.example.testplatform.report.vo.ProjectRowVO;
import com.example.testplatform.report.vo.TeamRowVO;

import java.util.List;

/**
 * 报表查询服务
 */
public interface ReportService {

    /**
     * 系统总览（卡片+环比+趋势+周期对照趋势）
     */
    OverviewVO systemOverview(String startDate, String endDate);

    /**
     * 团队报表行列表（含环比）
     */
    List<TeamRowVO> teamRows(String startDate, String endDate);

    /**
     * 团队下项目行列表（含环比）
     */
    List<ProjectRowVO> projectRows(String testTeam, String startDate, String endDate);

    /**
     * 项目总览（卡片+环比+趋势+周期对照趋势）
     */
    OverviewVO projectOverview(String projectId, String startDate, String endDate);

    /**
     * 用例明细分页（服务端排序）
     */
    PageResult<CaseRowVO> caseRows(String projectId, String startDate, String endDate,
                                   String sortField, String order, int pageNum, int pageSize,
                                   String keyword);

    /**
     * 失败TOP用例
     */
    List<CaseRowVO> topFailures(String projectId, String startDate, String endDate, int limit);

    /**
     * 用例明细全量行（导出用，含上限校验）
     */
    List<CaseRowVO> caseRowsForExport(String projectId, String startDate, String endDate,
                                      String keyword);
}
