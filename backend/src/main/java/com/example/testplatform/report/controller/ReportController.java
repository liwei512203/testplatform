package com.example.testplatform.report.controller;

import com.alibaba.excel.EasyExcel;
import com.example.testplatform.common.PageResult;
import com.example.testplatform.common.Result;
import com.example.testplatform.report.service.ReportService;
import com.example.testplatform.report.vo.CaseRowVO;
import com.example.testplatform.report.vo.OverviewVO;
import com.example.testplatform.report.vo.ProjectRowVO;
import com.example.testplatform.report.vo.TeamRowVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 报表接口：查询 + Excel 导出
 */
@Tag(name = "统计报表", description = "报表数据查询与 Excel 导出")
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // ==================== 查询接口 ====================

    @Operation(summary = "系统总览", description = "查询时间区间内的全系统执行统计总览")
    @GetMapping("/system/overview")
    public Result<OverviewVO> systemOverview(@RequestParam String startDate,
                                             @RequestParam String endDate) {
        return Result.ok(reportService.systemOverview(startDate, endDate));
    }

    @Operation(summary = "团队报表", description = "按测试团队维度统计执行数据")
    @GetMapping("/teams")
    public Result<List<TeamRowVO>> teamRows(@RequestParam String startDate,
                                            @RequestParam String endDate) {
        return Result.ok(reportService.teamRows(startDate, endDate));
    }

    @Operation(summary = "团队下项目报表", description = "查询某测试团队下各项目的执行统计")
    @GetMapping("/teams/{team}/projects")
    public Result<List<ProjectRowVO>> teamProjects(@PathVariable("team") String team,
                                                   @RequestParam String startDate,
                                                   @RequestParam String endDate) {
        return Result.ok(reportService.projectRows(team, startDate, endDate));
    }

    @Operation(summary = "项目总览", description = "查询某项目在时间区间内的执行统计总览")
    @GetMapping("/projects/{id}/overview")
    public Result<OverviewVO> projectOverview(@PathVariable("id") String id,
                                              @RequestParam String startDate,
                                              @RequestParam String endDate) {
        return Result.ok(reportService.projectOverview(id, startDate, endDate));
    }

    @Operation(summary = "项目用例明细", description = "分页查询某项目时间区间内的用例执行明细，支持排序与关键字过滤")
    @GetMapping("/projects/{id}/cases")
    public Result<PageResult<CaseRowVO>> projectCases(@PathVariable("id") String id,
                                                      @RequestParam String startDate,
                                                      @RequestParam String endDate,
                                                      @RequestParam(defaultValue = "fail") String sortField,
                                                      @RequestParam(defaultValue = "desc") String order,
                                                      @RequestParam(defaultValue = "1") int pageNum,
                                                      @RequestParam(defaultValue = "20") int pageSize,
                                                      @RequestParam(required = false) String keyword) {
        return Result.ok(reportService.caseRows(id, startDate, endDate, sortField, order,
                pageNum, pageSize, keyword));
    }

    @Operation(summary = "失败次数 Top 榜", description = "查询某项目时间区间内失败次数最多的用例")
    @GetMapping("/projects/{id}/top-failures")
    public Result<List<CaseRowVO>> topFailures(@PathVariable("id") String id,
                                               @RequestParam String startDate,
                                               @RequestParam String endDate,
                                               @RequestParam(defaultValue = "10") int limit) {
        return Result.ok(reportService.topFailures(id, startDate, endDate, limit));
    }

    // ==================== 导出接口 ====================

    @Operation(summary = "导出团队报表", description = "以 Excel 文件流导出团队维度报表（动态表头含各执行方式成功率列）")
    @GetMapping("/system/overview/export")
    public void exportTeams(@RequestParam String startDate,
                            @RequestParam String endDate,
                            HttpServletResponse response) throws IOException {
        List<TeamRowVO> rows = reportService.teamRows(startDate, endDate);
        // 动态表头：基础列 + 数据中出现的各执行方式成功率列
        List<String> typeKeys = collectTypeKeys(
                rows.stream().map(TeamRowVO::getTypeRates).collect(java.util.stream.Collectors.toList()));
        List<List<String>> head = new ArrayList<>();
        for (String h : Arrays.asList("测试团队", "项目数", "执行次数", "覆盖用例数", "总用例数",
                "执行率(%)", "成功次数", "失败次数", "成功率(%)", "执行环比(%)")) {
            head.add(Collections.singletonList(h));
        }
        for (String type : typeKeys) {
            head.add(Collections.singletonList(type + "成功率(%)"));
        }
        List<List<Object>> data = new ArrayList<>();
        for (TeamRowVO r : rows) {
            List<Object> line = new ArrayList<>(Arrays.asList(
                    r.getTestTeam(), r.getProjectCount(), r.getExecuted(), r.getCovered(),
                    r.getTotalCases(), r.getExecutionRate(), r.getSuccess(), r.getFail(),
                    r.getSuccessRate(), r.getExecutedChangeRate()));
            for (String type : typeKeys) {
                line.add(r.getTypeRates() == null ? null : r.getTypeRates().get(type));
            }
            data.add(line);
        }
        writeDynamicExcel(response, "团队报表", startDate, endDate, head, data, "团队报表");
    }

    @Operation(summary = "导出团队下项目报表", description = "以 Excel 文件流导出某团队下各项目报表")
    @GetMapping("/teams/{team}/projects/export")
    public void exportTeamProjects(@PathVariable("team") String team,
                                   @RequestParam String startDate,
                                   @RequestParam String endDate,
                                   HttpServletResponse response) throws IOException {
        List<ProjectRowVO> rows = reportService.projectRows(team, startDate, endDate);
        List<String> typeKeys = collectTypeKeys(
                rows.stream().map(ProjectRowVO::getTypeRates).collect(java.util.stream.Collectors.toList()));
        List<List<String>> head = new ArrayList<>();
        for (String h : Arrays.asList("项目名称", "系统编号", "测试团队", "执行次数", "覆盖用例数",
                "总用例数", "执行率(%)", "成功次数", "失败次数", "成功率(%)", "执行环比(%)")) {
            head.add(Collections.singletonList(h));
        }
        for (String type : typeKeys) {
            head.add(Collections.singletonList(type + "成功率(%)"));
        }
        List<List<Object>> data = new ArrayList<>();
        for (ProjectRowVO r : rows) {
            List<Object> line = new ArrayList<>(Arrays.asList(
                    r.getProjectName(), r.getSystemCode(), r.getTestTeam(), r.getExecuted(),
                    r.getCovered(), r.getTotalCases(), r.getExecutionRate(), r.getSuccess(),
                    r.getFail(), r.getSuccessRate(), r.getExecutedChangeRate()));
            for (String type : typeKeys) {
                line.add(r.getTypeRates() == null ? null : r.getTypeRates().get(type));
            }
            data.add(line);
        }
        writeDynamicExcel(response, "项目报表_" + team, startDate, endDate, head, data, "项目报表");
    }

    @Operation(summary = "导出项目用例明细", description = "以 Excel 文件流导出某项目时间区间内的用例执行明细")
    @GetMapping("/projects/{id}/cases/export")
    public void exportProjectCases(@PathVariable("id") String id,
                                   @RequestParam String startDate,
                                   @RequestParam String endDate,
                                   @RequestParam(required = false) String keyword,
                                   HttpServletResponse response) throws IOException {
        List<CaseRowVO> rows = reportService.caseRowsForExport(id, startDate, endDate, keyword);
        writeExcel(response, "用例明细", startDate, endDate, CaseRowVO.class, rows, "用例明细");
    }

    /**
     * 收集各行动态列的 key 并集（保持出现顺序），用于动态表头
     */
    private List<String> collectTypeKeys(List<Map<String, Double>> typeRatesList) {
        Set<String> keys = new LinkedHashSet<>();
        for (Map<String, Double> m : typeRatesList) {
            if (m != null) {
                keys.addAll(m.keySet());
            }
        }
        return new ArrayList<>(keys);
    }

    /**
     * 统一写出 Excel：文件名 {报表名}_{起始日}_{截止日}.xlsx
     */
    private <T> void writeExcel(HttpServletResponse response, String reportName,
                                String startDate, String endDate,
                                Class<T> clazz, List<T> rows, String sheetName) throws IOException {
        String fileName = reportName + "_" + startDate + "_" + endDate + ".xlsx";
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name())
                .replace("+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
        EasyExcel.write(response.getOutputStream(), clazz).sheet(sheetName).doWrite(rows);
    }

    /**
     * 动态表头写出 Excel（执行方式列随数据扩展）
     */
    private void writeDynamicExcel(HttpServletResponse response, String reportName,
                                   String startDate, String endDate,
                                   List<List<String>> head, List<List<Object>> data,
                                   String sheetName) throws IOException {
        String fileName = reportName + "_" + startDate + "_" + endDate + ".xlsx";
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name())
                .replace("+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
        EasyExcel.write(response.getOutputStream()).head(head).sheet(sheetName).doWrite(data);
    }
}
