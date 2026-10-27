package com.example.testplatform.task;

import com.example.testplatform.mapper.PartitionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 分区维护任务：
 * 1) 清理超过保留期的历史月分区（DROP PARTITION，秒级完成，替代大事务 DELETE）
 * 2) 滚动预建未来 N 个月的分区（REORGANIZE pmax）
 * 启动时执行一次 + 每日凌晨 2:30 定时执行（幂等）
 */
@Component
public class PartitionMaintenanceTask implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PartitionMaintenanceTask.class);

    /** 分区命名规则：pYYYYMM */
    private static final DateTimeFormatter PARTITION_FMT = DateTimeFormatter.ofPattern("yyyyMM");

    private static final Pattern NAME_PATTERN = Pattern.compile("^p(\\d{6})$");

    private final PartitionMapper partitionMapper;

    @Value("${report.stat-retention-months:36}")
    private int statRetentionMonths;

    @Value("${report.plan-retention-months:12}")
    private int planRetentionMonths;

    @Value("${report.partition-precreate-months:3}")
    private int precreateMonths;

    public PartitionMaintenanceTask(PartitionMapper partitionMapper) {
        this.partitionMapper = partitionMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        maintainAll();
    }

    @Scheduled(cron = "0 30 2 * * ?")
    public void scheduledMaintain() {
        maintainAll();
    }

    public void maintainAll() {
        // 用例日统计表：聚合数据，保留 36 个月
        maintain("execution_case_daily_stat", statRetentionMonths);
        // 计划用例明细表与执行日志表：明细/事实数据，保留 12 个月
        // （明细过期清理后，统计表仍保留汇总）
        maintain("plan_case", planRetentionMonths);
        maintain("case_execution_log", planRetentionMonths);
    }

    /**
     * 维护单张按月 RANGE COLUMNS 分区的表
     */
    void maintain(String tableName, int retentionMonths) {
        try {
            List<Map<String, Object>> partitions = partitionMapper.selectPartitions(tableName);
            Set<YearMonth> existing = new HashSet<>();
            boolean hasPmax = false;
            for (Map<String, Object> p : partitions) {
                String name = String.valueOf(p.get("partitionName"));
                if ("pmax".equals(name)) {
                    hasPmax = true;
                    continue;
                }
                Matcher m = NAME_PATTERN.matcher(name);
                if (m.matches()) {
                    existing.add(YearMonth.parse(m.group(1), PARTITION_FMT));
                }
            }

            YearMonth now = YearMonth.now();
            YearMonth cutoff = now.minusMonths(retentionMonths);

            // 1) 清理超过保留期的历史分区
            for (YearMonth ym : existing) {
                if (ym.isBefore(cutoff)) {
                    String ddl = "ALTER TABLE `" + tableName + "` DROP PARTITION `p"
                            + ym.format(PARTITION_FMT) + "`";
                    partitionMapper.executeDdl(ddl);
                    log.warn("分区清理：表 {} 已删除过期分区 p{}", tableName, ym.format(PARTITION_FMT));
                }
            }

            Set<YearMonth> kept = new HashSet<>();
            for (YearMonth ym : existing) {
                if (!ym.isBefore(cutoff)) {
                    kept.add(ym);
                }
            }

            // 2) 滚动预建未来分区（不重建已存在的月份）
            YearMonth maxKept = kept.stream().max(Comparator.naturalOrder())
                    .orElse(cutoff.minusMonths(1));
            YearMonth target = now.plusMonths(precreateMonths);
            List<YearMonth> toAdd = new ArrayList<>();
            for (YearMonth m = maxKept.plusMonths(1); !m.isAfter(target); m = m.plusMonths(1)) {
                toAdd.add(m);
            }
            if (!toAdd.isEmpty()) {
                if (!hasPmax) {
                    log.error("表 {} 缺少 pmax 分区，无法预建新分区", tableName);
                    return;
                }
                StringBuilder sb = new StringBuilder();
                sb.append("ALTER TABLE `").append(tableName)
                        .append("` REORGANIZE PARTITION `pmax` INTO (");
                for (YearMonth m : toAdd) {
                    String upperBound = m.plusMonths(1).atDay(1).toString();
                    sb.append("PARTITION `p").append(m.format(PARTITION_FMT))
                            .append("` VALUES LESS THAN ('").append(upperBound).append("'), ");
                }
                sb.append("PARTITION `pmax` VALUES LESS THAN (MAXVALUE))");
                partitionMapper.executeDdl(sb.toString());
                log.info("分区预建：表 {} 新增 {} 个分区（至 {}）", tableName, toAdd.size(),
                        target.format(PARTITION_FMT));
            }
        } catch (Exception e) {
            log.error("分区维护失败：表 " + tableName, e);
        }
    }
}
