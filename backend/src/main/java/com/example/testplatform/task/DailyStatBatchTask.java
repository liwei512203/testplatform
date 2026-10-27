package com.example.testplatform.task;

import com.example.testplatform.mapper.DailyStatMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 每日统计跑批任务：
 *   每天凌晨从执行日志（case_execution_log）重算最近 N 天的两张日统计表。
 * 设计要点：
 * 1) 执行路径不再实时维护统计表（只更新状态 + 追加日志），本跑批是统计表唯一更新来源
 * 2) 重算窗口默认 2 天（昨天 + 前天）：
 *    - 覆盖跑批时刻附近跨天执行的边界数据
 *    - 跑批当天服务重启/漏跑时，窗口重算可自动补齐（幂等，先删后插）
 * 3) 每个日期的统计 = 该日期日志的完整聚合，天然自愈（删除计划/改期/删用例
 *    等操作导致的统计滞后，最迟在下一轮跑批自动修正）
 */
@Component
public class DailyStatBatchTask {

    private static final Logger log = LoggerFactory.getLogger(DailyStatBatchTask.class);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final DailyStatMapper dailyStatMapper;

    @Value("${report.stat-batch.window-days:2}")
    private int windowDays;

    public DailyStatBatchTask(DailyStatMapper dailyStatMapper) {
        this.dailyStatMapper = dailyStatMapper;
    }

    @Scheduled(cron = "${report.stat-batch.cron:0 0 1 * * ?}")
    public void runBatch() {
        log.info("每日统计跑批开始，重算窗口：{} 天", windowDays);
        long startTime = System.currentTimeMillis();
        int success = 0;
        int failed = 0;
        for (int i = 1; i <= Math.max(windowDays, 1); i++) {
            String statDate = LocalDate.now().minusDays(i).format(DATE_FMT);
            try {
                refreshOneDate(statDate);
                success++;
            } catch (Exception e) {
                failed++;
                log.error("统计跑批失败：日期 " + statDate, e);
            }
        }
        log.info("每日统计跑批结束：成功 {} 天，失败 {} 天，耗时 {} ms",
                success, failed, System.currentTimeMillis() - startTime);
    }

    /**
     * 重算单个日期的统计（两张表，先删后插）
     */
    public void refreshOneDate(String statDate) {
        dailyStatMapper.deleteStatByDate(statDate);
        dailyStatMapper.insertStatByDate(statDate);
        dailyStatMapper.deleteCaseStatByDate(statDate);
        dailyStatMapper.insertCaseStatByDate(statDate);
    }

    /**
     * 全量重算：自动发现日志数据的日期范围，逐日重算全部统计。
     * 适用于：历史数据结构变更后的整体重建、全量对账、统计表损坏恢复。
     *
     * @return 处理结果描述（起止日期、天数、失败数）
     */
    public String refreshAll() {
        Map<String, Object> range = dailyStatMapper.selectExecDateRange();
        if (range == null || range.get("minDate") == null || range.get("maxDate") == null) {
            return "执行日志中无数据，无需重算";
        }
        LocalDate start = toLocalDate(range.get("minDate"));
        LocalDate end = toLocalDate(range.get("maxDate"));
        log.warn("全量统计重算开始：{} ~ {}", start, end);
        long startTime = System.currentTimeMillis();
        int days = 0;
        int failed = 0;
        List<String> failDates = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            days++;
            try {
                refreshOneDate(d.format(DATE_FMT));
            } catch (Exception e) {
                failed++;
                failDates.add(d.format(DATE_FMT));
                log.error("全量重算失败：日期 " + d, e);
            }
        }
        String msg = "全量重算完成：" + start + " ~ " + end + " 共 " + days + " 天，失败 " + failed + " 天"
                + (failed > 0 ? "（" + String.join(",", failDates) + "）" : "")
                + "，耗时 " + (System.currentTimeMillis() - startTime) + " ms";
        log.warn(msg);
        return msg;
    }

    /**
     * SQL date 类型（java.sql.Date / LocalDate）统一转 LocalDate
     */
    private LocalDate toLocalDate(Object sqlDate) {
        if (sqlDate == null) {
            return null;
        }
        if (sqlDate instanceof LocalDate) {
            return (LocalDate) sqlDate;
        }
        return ((java.sql.Date) sqlDate).toLocalDate();
    }
}
