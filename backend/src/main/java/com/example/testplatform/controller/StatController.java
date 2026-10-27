package com.example.testplatform.controller;

import com.example.testplatform.common.Result;
import com.example.testplatform.task.DailyStatBatchTask;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 统计管理接口：手动触发统计重算。
 * 平时由每日跑批自动维护（凌晨重算最近2天），无需调用；
 * 本接口用于验证报表数据准确性、补数、立即刷新。
 */
@Tag(name = "统计管理", description = "手动触发统计重算；平时由每日跑批自动维护，接口用于验证报表数据、补数、立即刷新")
@RestController
@RequestMapping("/api/stats")
public class StatController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final DailyStatBatchTask dailyStatBatchTask;

    @Value("${report.range.max-days:366}")
    private int maxRangeDays;

    @Autowired
    public StatController(DailyStatBatchTask dailyStatBatchTask) {
        this.dailyStatBatchTask = dailyStatBatchTask;
    }

    /**
     * 手动重算统计：
     * 1) 不传参数：重算今天（等价执行路径数据的即时入账）
     * 2) 传 date：重算指定单日
     * 3) 传 startDate + endDate：重算区间内每一天（用于验证整段时间报表准确性）
     *    区间跨度上限同报表（默认366天）
     */
    @Operation(summary = "手动重算统计", description = "不传参数重算今天；传 date 重算指定单日；传 startDate + endDate 重算区间内每一天（上限默认366天）")
    @PostMapping("/refresh")
    public Result<String> refresh(@RequestParam(required = false) String date,
                                  @RequestParam(required = false) String startDate,
                                  @RequestParam(required = false) String endDate) {
        LocalDate start;
        LocalDate end;
        try {
            if (startDate != null && !startDate.isEmpty()) {
                start = LocalDate.parse(startDate, DATE_FMT);
                end = endDate == null || endDate.isEmpty() ? start : LocalDate.parse(endDate, DATE_FMT);
            } else if (date != null && !date.isEmpty()) {
                start = LocalDate.parse(date, DATE_FMT);
                end = start;
            } else {
                start = LocalDate.now();
                end = start;
            }
        } catch (Exception e) {
            return Result.fail(400, "日期格式不合法，应为 yyyy-MM-dd");
        }
        if (start.isAfter(end)) {
            return Result.fail(400, "开始日期不能晚于结束日期");
        }
        if (start.plusDays(maxRangeDays).isBefore(end)) {
            return Result.fail(400, "重算区间不能超过 " + maxRangeDays + " 天");
        }

        int days = 0;
        int failed = 0;
        StringBuilder failDates = new StringBuilder();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            days++;
            try {
                dailyStatBatchTask.refreshOneDate(d.toString());
            } catch (Exception e) {
                failed++;
                if (failDates.length() > 0) {
                    failDates.append(",");
                }
                failDates.append(d);
            }
        }
        String msg = "已重算 " + start + " ~ " + end + " 共 " + days + " 天的统计数据";
        if (failed > 0) {
            msg += "，失败 " + failed + " 天（" + failDates + "），详见后端日志";
        }
        return Result.ok(msg);
    }

    /**
     * 全量重算：不传任何参数，自动扫描执行日志的全部日期范围，逐日重建统计表。
     * 适用于历史数据结构变更后的整体重建、全量对账。
     * 注意：数据量大时耗时较长（同步执行），请避免在业务高峰期调用。
     */
    @Operation(summary = "全量重算统计", description = "扫描执行日志全部日期范围逐日重建统计表；数据量大时耗时较长，避免业务高峰期调用")
    @PostMapping("/refresh-all")
    public Result<String> refreshAll() {
        return Result.ok(dailyStatBatchTask.refreshAll());
    }
}
