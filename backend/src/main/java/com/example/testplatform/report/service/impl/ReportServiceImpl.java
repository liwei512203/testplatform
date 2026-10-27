package com.example.testplatform.report.service.impl;

import com.example.testplatform.common.PageResult;
import com.example.testplatform.exception.BizException;
import com.example.testplatform.mapper.ProjectMapper;
import com.example.testplatform.report.mapper.ReportMapper;
import com.example.testplatform.report.service.ReportService;
import com.example.testplatform.report.vo.CaseRowVO;
import com.example.testplatform.report.vo.OverviewVO;
import com.example.testplatform.report.vo.ProjectRowVO;
import com.example.testplatform.report.vo.StatCardVO;
import com.example.testplatform.report.vo.TeamRowVO;
import com.example.testplatform.report.vo.TypeStatVO;
import com.example.testplatform.report.vo.TrendPointVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 报表查询服务实现
 */
@Service
public class ReportServiceImpl implements ReportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    /** 明细排序白名单：请求参数 -> SQL 排序子句 */
    private static final Map<String, String> SORT_CLAUSES = new HashMap<>();

    /** 周粒度阈值（含） */
    private static final int DAY_GRANULARITY_MAX = 60;

    static {
        SORT_CLAUSES.put("fail_desc", "fail DESC, executed DESC");
        SORT_CLAUSES.put("rate_asc", "success/executed ASC, fail DESC");
        SORT_CLAUSES.put("executed_desc", "executed DESC, fail DESC");
        SORT_CLAUSES.put("lastDate_desc", "lastDate DESC, fail DESC");
    }

    private final ReportMapper reportMapper;

    private final ProjectMapper projectMapper;

    @Value("${report.range.max-days:366}")
    private int maxRangeDays;

    @Value("${report.export.max-rows:100000}")
    private int exportMaxRows;

    @Autowired
    public ReportServiceImpl(ReportMapper reportMapper, ProjectMapper projectMapper) {
        this.reportMapper = reportMapper;
        this.projectMapper = projectMapper;
    }

    // ==================== 系统总览 ====================

    @Override
    public OverviewVO systemOverview(String startDate, String endDate) {
        LocalDate start = parseAndCheck(startDate, endDate);
        LocalDate end = LocalDate.parse(endDate, DATE_FMT);
        LocalDate prevStart = start.minusDays(daysBetween(start, end) + 1);
        LocalDate prevEnd = start.minusDays(1);

        OverviewVO vo = new OverviewVO();
        vo.setDataThroughDate(dataThroughDate());

        // 当前区间汇总
        Map<String, Object> cur = orEmpty(reportMapper.sumDaily(null, null, startDate, endDate));
        Map<String, Object> prev = orEmpty(reportMapper.sumDaily(null, null,
                prevStart.format(DATE_FMT), prevEnd.format(DATE_FMT)));
        long curExecuted = asLong(cur.get("executed"));
        long curSuccess = asLong(cur.get("success"));
        long curFail = asLong(cur.get("fail"));
        long prevExecuted = asLong(prev.get("executed"));
        long prevSuccess = asLong(prev.get("success"));
        long prevFail = asLong(prev.get("fail"));
        boolean prevHasData = prevExecuted + prevSuccess + prevFail > 0;

        long covered = reportMapper.countCoveredCases(null, null, startDate, endDate);

        List<StatCardVO> cards = new ArrayList<>();
        cards.add(new StatCardVO("projectCount", "项目总数",
                projectMapper.selectAll(null, null).size(), "", null, null, "up-good"));
        cards.add(card("executed", "执行次数", curExecuted, "", prevHasData ? (double) prevExecuted : null, "up-good"));
        cards.add(card("covered", "覆盖用例数", covered, "", "up-good"));
        cards.add(executionRateCard(covered, reportMapper.countTotalCases(null, null)));
        cards.add(rateCard("successRate", "整体成功率", curSuccess, curExecuted,
                prevHasData ? prevSuccess : null, prevExecuted));
        cards.add(card("fail", "失败次数", curFail, "", prevHasData ? (double) prevFail : null, "down-good"));
        vo.setCards(cards);

        vo.setTypeStats(toTypeStats(reportMapper.sumByExecType(null, null, startDate, endDate)));

        fillTrend(vo, null, null, startDate, endDate, prevStart.format(DATE_FMT), prevEnd.format(DATE_FMT));
        return vo;
    }

    // ==================== 团队报表 ====================

    @Override
    public List<TeamRowVO> teamRows(String startDate, String endDate) {
        LocalDate start = parseAndCheck(startDate, endDate);
        LocalDate end = LocalDate.parse(endDate, DATE_FMT);
        String prevStart = start.minusDays(daysBetween(start, end) + 1).format(DATE_FMT);
        String prevEnd = start.minusDays(1).format(DATE_FMT);

        // 团队汇总（含上一周期执行数对照）
        List<Map<String, Object>> sums = reportMapper.sumByTeam(startDate, endDate, prevStart, prevEnd);
        // 团队覆盖用例数
        Map<String, Long> coveredMap = new HashMap<>();
        for (Map<String, Object> m : reportMapper.coveredByTeam(startDate, endDate)) {
            coveredMap.put(String.valueOf(m.get("testTeam")), asLong(m.get("covered")));
        }
        // 团队项目数（当前存在项目）
        Map<String, Long> projectCountMap = new HashMap<>();
        for (com.example.testplatform.entity.Project p : projectMapper.selectAll(null, null)) {
            projectCountMap.merge(p.getTestTeam(), 1L, Long::sum);
        }
        // 团队总用例数（当前存量）
        Map<String, Long> totalCasesMap = new HashMap<>();
        for (Map<String, Object> m : reportMapper.totalCasesByTeam()) {
            totalCasesMap.put(String.valueOf(m.get("testTeam")), asLong(m.get("totalCases")));
        }
        // 团队 x 执行方式 成功率（动态结构，值域随数据/字典变化）
        Map<String, Map<String, Double>> teamTypeRates = new HashMap<>();
        for (Map<String, Object> m : reportMapper.sumByTeamAndType(startDate, endDate)) {
            String team = String.valueOf(m.get("testTeam"));
            long executed = asLong(m.get("executed"));
            long success = asLong(m.get("success"));
            teamTypeRates.computeIfAbsent(team, k -> new LinkedHashMap<>())
                    .put(String.valueOf(m.get("execType")), rate(success, executed));
        }

        List<TeamRowVO> rows = new ArrayList<>();
        for (Map<String, Object> m : sums) {
            TeamRowVO row = new TeamRowVO();
            row.setTestTeam(String.valueOf(m.get("testTeam")));
            row.setProjectCount(projectCountMap.getOrDefault(row.getTestTeam(), 0L));
            long executed = asLong(m.get("executed"));
            row.setExecuted(executed);
            long covered = coveredMap.getOrDefault(row.getTestTeam(), 0L);
            row.setCovered(covered);
            long totalCases = totalCasesMap.getOrDefault(row.getTestTeam(), 0L);
            row.setTotalCases(totalCases);
            row.setExecutionRate(totalCases > 0 ? round(covered * 100.0 / totalCases) : null);
            long success = asLong(m.get("success"));
            long fail = asLong(m.get("fail"));
            row.setSuccess(success);
            row.setFail(fail);
            row.setSuccessRate(rate(success, executed));
            row.setTypeRates(teamTypeRates.getOrDefault(row.getTestTeam(),
                    Collections.emptyMap()));
            Long prevExecuted = asNullableLong(m.get("prevExecuted"));
            if (prevExecuted != null && prevExecuted > 0) {
                row.setPrevExecuted(prevExecuted);
                row.setExecutedChangeRate((executed - prevExecuted) * 100.0 / prevExecuted);
                row.setPrevSuccessRate(null);
            }
            rows.add(row);
        }
        // 补充有项目但无执行的团队（显示 0 值行）
        for (Map.Entry<String, Long> e : projectCountMap.entrySet()) {
            boolean exists = rows.stream().anyMatch(r -> r.getTestTeam().equals(e.getKey()));
            if (!exists) {
                TeamRowVO row = new TeamRowVO();
                row.setTestTeam(e.getKey());
                row.setProjectCount(e.getValue());
                long totalCases = totalCasesMap.getOrDefault(e.getKey(), 0L);
                row.setTotalCases(totalCases);
                row.setExecutionRate(totalCases > 0 ? 0.0 : null);
                rows.add(row);
            }
        }
        rows.sort((a, b) -> Long.compare(b.getExecuted(), a.getExecuted()));
        return rows;
    }

    @Override
    public List<ProjectRowVO> projectRows(String testTeam, String startDate, String endDate) {
        LocalDate start = parseAndCheck(startDate, endDate);
        LocalDate end = LocalDate.parse(endDate, DATE_FMT);
        String prevStart = start.minusDays(daysBetween(start, end) + 1).format(DATE_FMT);
        String prevEnd = start.minusDays(1).format(DATE_FMT);

        List<Map<String, Object>> rows = reportMapper.projectRows(testTeam, startDate, endDate,
                prevStart, prevEnd);
        Map<String, Long> coveredMap = new HashMap<>();
        for (Map<String, Object> m : reportMapper.coveredByProject(testTeam, startDate, endDate)) {
            coveredMap.put(String.valueOf(m.get("projectId")), asLong(m.get("covered")));
        }
        // 项目总用例数（当前存量）
        Map<String, Long> totalCasesMap = new HashMap<>();
        for (Map<String, Object> m : reportMapper.totalCasesByProject(testTeam)) {
            totalCasesMap.put(String.valueOf(m.get("projectId")), asLong(m.get("totalCases")));
        }
        // 项目 x 执行方式 成功率（动态结构）
        Map<String, Map<String, Double>> projectTypeRates = new HashMap<>();
        for (Map<String, Object> m : reportMapper.sumByProjectAndType(testTeam, startDate, endDate)) {
            String pid = String.valueOf(m.get("projectId"));
            long executed = asLong(m.get("executed"));
            long success = asLong(m.get("success"));
            projectTypeRates.computeIfAbsent(pid, k -> new LinkedHashMap<>())
                    .put(String.valueOf(m.get("execType")), rate(success, executed));
        }

        List<ProjectRowVO> result = new ArrayList<>();
        for (Map<String, Object> m : rows) {
            ProjectRowVO row = new ProjectRowVO();
            row.setProjectId(String.valueOf(m.get("projectId")));
            row.setProjectName(String.valueOf(m.get("projectName")));
            row.setSystemCode(String.valueOf(m.get("systemCode")));
            row.setTestTeam(String.valueOf(m.get("testTeam")));
            long executed = asLong(m.get("executed"));
            long success = asLong(m.get("success"));
            long fail = asLong(m.get("fail"));
            row.setExecuted(executed);
            row.setSuccess(success);
            row.setFail(fail);
            long covered = coveredMap.getOrDefault(row.getProjectId(), 0L);
            row.setCovered(covered);
            long totalCases = totalCasesMap.getOrDefault(row.getProjectId(), 0L);
            row.setTotalCases(totalCases);
            row.setExecutionRate(totalCases > 0 ? round(covered * 100.0 / totalCases) : null);
            row.setSuccessRate(rate(success, executed));
            row.setTypeRates(projectTypeRates.getOrDefault(row.getProjectId(),
                    Collections.emptyMap()));
            Long prevExecuted = asNullableLong(m.get("prevExecuted"));
            if (prevExecuted != null && prevExecuted > 0) {
                row.setPrevExecuted(prevExecuted);
                row.setExecutedChangeRate((executed - prevExecuted) * 100.0 / prevExecuted);
            }
            result.add(row);
        }
        return result;
    }

    // ==================== 项目总览 ====================

    @Override
    public OverviewVO projectOverview(String projectId, String startDate, String endDate) {
        checkProjectExists(projectId);
        LocalDate start = parseAndCheck(startDate, endDate);
        LocalDate end = LocalDate.parse(endDate, DATE_FMT);
        LocalDate prevStart = start.minusDays(daysBetween(start, end) + 1);
        LocalDate prevEnd = start.minusDays(1);

        OverviewVO vo = new OverviewVO();
        vo.setDataThroughDate(dataThroughDate());

        Map<String, Object> cur = orEmpty(reportMapper.sumDaily(projectId, null, startDate, endDate));
        Map<String, Object> prev = orEmpty(reportMapper.sumDaily(projectId, null,
                prevStart.format(DATE_FMT), prevEnd.format(DATE_FMT)));
        long curExecuted = asLong(cur.get("executed"));
        long curSuccess = asLong(cur.get("success"));
        long curFail = asLong(cur.get("fail"));
        long prevExecuted = asLong(prev.get("executed"));
        long prevSuccess = asLong(prev.get("success"));
        long prevFail = asLong(prev.get("fail"));
        boolean prevHasData = prevExecuted + prevSuccess + prevFail > 0;

        long covered = reportMapper.countCoveredCases(projectId, null, startDate, endDate);

        List<StatCardVO> cards = new ArrayList<>();
        cards.add(card("executed", "执行次数", curExecuted, "", prevHasData ? (double) prevExecuted : null, "up-good"));
        cards.add(card("covered", "覆盖用例数", covered, "", "up-good"));
        cards.add(executionRateCard(covered, reportMapper.countTotalCases(projectId, null)));
        cards.add(rateCard("successRate", "整体成功率", curSuccess, curExecuted,
                prevHasData ? prevSuccess : null, prevExecuted));
        cards.add(card("fail", "失败次数", curFail, "", prevHasData ? (double) prevFail : null, "down-good"));
        vo.setCards(cards);

        vo.setTypeStats(toTypeStats(reportMapper.sumByExecType(projectId, null, startDate, endDate)));

        fillTrend(vo, projectId, null, startDate, endDate,
                prevStart.format(DATE_FMT), prevEnd.format(DATE_FMT));
        return vo;
    }

    // ==================== 用例明细 ====================

    @Override
    public PageResult<CaseRowVO> caseRows(String projectId, String startDate, String endDate,
                                          String sortField, String order, int pageNum, int pageSize,
                                          String keyword) {
        checkProjectExists(projectId);
        parseAndCheck(startDate, endDate);
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 20;
        }
        if (pageSize > 200) {
            pageSize = 200;
        }
        String orderClause = resolveOrderClause(sortField, order);
        long total = reportMapper.countCaseRows(projectId, startDate, endDate, keyword);
        List<Map<String, Object>> rows = reportMapper.caseRows(projectId, startDate, endDate,
                keyword, orderClause, (pageNum - 1) * pageSize, pageSize);
        // 分执行方式成功率（动态值域）
        Map<Long, Map<String, Double>> caseTypeRates = loadCaseTypeRates(projectId, startDate, endDate);
        List<CaseRowVO> voList = toCaseRows(rows);
        for (CaseRowVO vo : voList) {
            vo.setTypeRates(caseTypeRates.getOrDefault(vo.getCaseId(), Collections.emptyMap()));
        }
        return new PageResult<>(total, voList);
    }

    @Override
    public List<CaseRowVO> topFailures(String projectId, String startDate, String endDate, int limit) {
        checkProjectExists(projectId);
        parseAndCheck(startDate, endDate);
        if (limit < 1 || limit > 100) {
            limit = 10;
        }
        return toCaseRows(reportMapper.topFailures(projectId, startDate, endDate, limit));
    }

    @Override
    public List<CaseRowVO> caseRowsForExport(String projectId, String startDate, String endDate,
                                             String keyword) {
        checkProjectExists(projectId);
        parseAndCheck(startDate, endDate);
        long total = reportMapper.countCaseRows(projectId, startDate, endDate, keyword);
        if (total > exportMaxRows) {
            throw new BizException("导出行数 " + total + " 超过上限 " + exportMaxRows + "，请收窄时间范围");
        }
        List<CaseRowVO> voList = toCaseRows(reportMapper.caseRows(projectId, startDate, endDate,
                keyword, "fail DESC, executed DESC", 0, (int) total));
        Map<Long, Map<String, Double>> caseTypeRates = loadCaseTypeRates(projectId, startDate, endDate);
        for (CaseRowVO vo : voList) {
            vo.setTypeRates(caseTypeRates.getOrDefault(vo.getCaseId(), Collections.emptyMap()));
        }
        return voList;
    }

    /**
     * 项目下 用例 x 执行方式 成功率（透视，key=caseId）
     */
    private Map<Long, Map<String, Double>> loadCaseTypeRates(String projectId,
                                                             String startDate, String endDate) {
        Map<Long, Map<String, Double>> map = new HashMap<>();
        for (Map<String, Object> m : reportMapper.sumCaseByProjectAndType(projectId, startDate, endDate)) {
            Long caseId = asNullableLong(m.get("caseId"));
            if (caseId == null) {
                continue;
            }
            long executed = asLong(m.get("executed"));
            long success = asLong(m.get("success"));
            map.computeIfAbsent(caseId, k -> new LinkedHashMap<>())
                    .put(String.valueOf(m.get("execType")), rate(success, executed));
        }
        return map;
    }

    // ==================== 私有工具 ====================

    /**
     * 校验并解析日期：格式合法、start<=end、跨度<=maxRangeDays
     */
    private LocalDate parseAndCheck(String startDate, String endDate) {
        if (startDate == null || endDate == null) {
            throw new BizException("startDate 与 endDate 不能为空");
        }
        LocalDate start;
        LocalDate end;
        try {
            start = LocalDate.parse(startDate, DATE_FMT);
            end = LocalDate.parse(endDate, DATE_FMT);
        } catch (Exception e) {
            throw new BizException("日期格式不合法，应为 yyyy-MM-dd");
        }
        if (start.isAfter(end)) {
            throw new BizException("开始日期不能晚于结束日期");
        }
        if (daysBetween(start, end) + 1 > maxRangeDays) {
            throw new BizException("时间范围不能超过 " + maxRangeDays + " 天");
        }
        return start;
    }

    /**
     * 填充趋势：<=60天按日，否则按周；同时取上一对照周期
     */
    private void fillTrend(OverviewVO vo, String projectId, String testTeam,
                           String startDate, String endDate,
                           String prevStart, String prevEnd) {
        long days = daysBetween(LocalDate.parse(startDate, DATE_FMT),
                LocalDate.parse(endDate, DATE_FMT)) + 1;
        boolean byWeek = days > DAY_GRANULARITY_MAX;
        vo.setGranularity(byWeek ? "week" : "day");
        List<Map<String, Object>> cur = byWeek
                ? reportMapper.trendByWeek(projectId, testTeam, startDate, endDate)
                : reportMapper.trendByDay(projectId, testTeam, startDate, endDate);
        List<Map<String, Object>> prev = byWeek
                ? reportMapper.trendByWeek(projectId, testTeam, prevStart, prevEnd)
                : reportMapper.trendByDay(projectId, testTeam, prevStart, prevEnd);
        vo.setTrend(toTrendPoints(cur));
        vo.setPrevTrend(toTrendPoints(prev));
    }

    private List<TrendPointVO> toTrendPoints(List<Map<String, Object>> rows) {
        List<TrendPointVO> points = new ArrayList<>();
        if (rows == null) {
            return points;
        }
        for (Map<String, Object> m : rows) {
            long executed = asLong(m.get("executed"));
            long success = asLong(m.get("success"));
            long fail = asLong(m.get("fail"));
            Object date = m.get("date");
            points.add(new TrendPointVO(
                    date == null ? "" : String.valueOf(date),
                    executed, success, fail, rate(success, executed)));
        }
        return points;
    }

    /**
     * 分执行方式统计行：完全由数据驱动（字典中新增执行方式且有数据即出现）
     */
    private List<TypeStatVO> toTypeStats(List<Map<String, Object>> rows) {
        List<TypeStatVO> list = new ArrayList<>();
        if (rows == null) {
            return list;
        }
        for (Map<String, Object> m : rows) {
            long executed = asLong(m.get("executed"));
            long success = asLong(m.get("success"));
            list.add(new TypeStatVO(String.valueOf(m.get("execType")),
                    executed, success, asLong(m.get("fail")), rate(success, executed)));
        }
        return list;
    }

    private List<CaseRowVO> toCaseRows(List<Map<String, Object>> rows) {
        List<CaseRowVO> list = new ArrayList<>();
        if (rows == null) {
            return list;
        }
        for (Map<String, Object> m : rows) {
            CaseRowVO row = new CaseRowVO();
            row.setCaseId(asNullableLong(m.get("caseId")));
            Object name = m.get("caseName");
            row.setCaseName(name == null ? "" : String.valueOf(name));
            long executed = asLong(m.get("executed"));
            long success = asLong(m.get("success"));
            row.setExecuted(executed);
            row.setSuccess(success);
            row.setFail(asLong(m.get("fail")));
            row.setSuccessRate(rate(success, executed));
            Object last = m.get("lastDate");
            row.setLastExecDate(last == null ? null : String.valueOf(last));
            list.add(row);
        }
        return list;
    }

    /**
     * 排序白名单：sortField(fail/rate/executed/lastDate) + order(desc/asc)
     */
    private String resolveOrderClause(String sortField, String order) {
        String field = sortField == null ? "fail" : sortField;
        String ord = "asc".equalsIgnoreCase(order) ? "asc" : "desc";
        if ("rate".equals(field) && "desc".equals(ord)) {
            // 成功率降序 = 失败率升序
            return "success/executed DESC, fail ASC";
        }
        String key = field + "_" + ord;
        String clause = SORT_CLAUSES.get(key);
        if (clause == null) {
            throw new BizException("不支持的排序字段：" + sortField);
        }
        return clause;
    }

    private StatCardVO card(String key, String label, long value, String suffix,
                            Double prevValue, String direction) {
        Double change = null;
        if (prevValue != null && prevValue != 0) {
            change = (value - prevValue) * 100.0 / prevValue;
        }
        return new StatCardVO(key, label, value, suffix, prevValue, round(change), direction);
    }

    /** 无对照数据的卡片（覆盖用例数等暂不做环比） */
    private StatCardVO card(String key, String label, long value, String suffix, String direction) {
        return card(key, label, value, suffix, null, direction);
    }

    /** 执行率卡片：被执行的去重用例数 / 总用例数（当前存量），无环比 */
    private StatCardVO executionRateCard(long covered, long totalCases) {
        Double value = totalCases > 0 ? round(covered * 100.0 / totalCases) : null;
        return new StatCardVO("executionRate", "执行率",
                value == null ? 0 : value, "%", null, null, "up-good");
    }

    private StatCardVO rateCard(String key, String label, long success, long executed,
                                Long prevSuccess, long prevExecuted) {
        Double value = rate(success, executed);
        Double prev = (prevSuccess != null && prevExecuted > 0)
                ? rate(prevSuccess, prevExecuted) : null;
        Double change = null;
        if (value != null && prev != null) {
            change = round(value - prev);
        }
        return new StatCardVO(key, label, value == null ? 0 : value, "%", prev, change, "up-good");
    }

    private Double rate(long success, long executed) {
        if (executed <= 0) {
            return null;
        }
        return round(success * 100.0 / executed);
    }

    private double round(Double v) {
        if (v == null) {
            return 0;
        }
        return Math.round(v * 100) / 100.0;
    }

    private String dataThroughDate() {
        // T+1 跑批口径：数据截至昨天
        return LocalDate.now().minusDays(1).format(DATE_FMT);
    }

    private long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    private void checkProjectExists(String projectId) {
        if (projectId == null || projectMapper.selectById(projectId) == null) {
            throw new BizException("项目不存在：" + projectId);
        }
    }

    private long asLong(Object v) {
        return v == null ? 0L : ((Number) v).longValue();
    }

    private Long asNullableLong(Object v) {
        return v == null ? null : ((Number) v).longValue();
    }

    private Map<String, Object> orEmpty(Map<String, Object> m) {
        return m == null ? Collections.emptyMap() : m;
    }
}
