-- =========================================================
-- 一、报表场景的表结构优化
-- =========================================================

-- 1. 版本计划增加 (项目, 计划时间) 复合索引，加速时间范围查询
ALTER TABLE `version_plan`
  ADD INDEX `idx_project_create`(`project_id`, `create_time`);

-- 2. 用例执行每日统计汇总表（预聚合，报表唯一数据源）
--    报表按 天 × 项目 粒度聚合，团队字段冗余存储，团队/系统/项目报表均查此表
CREATE TABLE `execution_daily_stat` (
  `stat_date` date NOT NULL COMMENT '统计日期',
  `project_id` varchar(50) NOT NULL COMMENT '项目ID',
  `test_team` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属测试团队（冗余，便于团队报表）',
  `executed_count` bigint NOT NULL DEFAULT 0 COMMENT '执行用例数（成功+失败）',
  `success_count` bigint NOT NULL DEFAULT 0 COMMENT '执行成功数',
  `fail_count` bigint NOT NULL DEFAULT 0 COMMENT '执行失败数',
  PRIMARY KEY (`stat_date`, `project_id`),
  KEY `idx_project_date`(`project_id`, `stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用例执行每日统计（报表数据源）';

-- =========================================================
-- 二、批量版本计划数据
--    所有项目各 50 个版本计划，计划时间在
--    2026-01-01 00:00:00 ~ 2026-10-25 23:59:59 内随机分布；
--    每个计划继承项目全部用例并随机分配执行结果（80%成功/20%失败），
--    执行时间与计划时间保持一致
-- =========================================================

-- 3. 批量插入版本计划（LIMIT 强制物化派生表，保证 RAND 每行只计算一次）
INSERT INTO `version_plan` (`project_id`, `name`, `create_time`, `update_time`)
SELECT t.project_id, t.name, t.ct, t.ct
FROM (
  SELECT
    p.`id` AS project_id,
    CONCAT('批量V', LPAD(s.n, 2, '0')) AS name,
    (UNIX_TIMESTAMP('2026-01-01 00:00:00')
      + FLOOR(RAND() * (UNIX_TIMESTAMP('2026-10-25 23:59:59')
                        - UNIX_TIMESTAMP('2026-01-01 00:00:00')))) * 1000 AS ct
  FROM `project` p
  CROSS JOIN (
    SELECT (a.i - 1) * 10 + b.j AS n
    FROM (SELECT 1 AS i UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) a,
         (SELECT 1 AS j UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
          UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) b
  ) s
  LIMIT 100000
) t;

-- 4. 批量计划继承项目全部用例并随机分配执行结果，执行时间 = 计划时间
INSERT INTO `plan_case` (`plan_id`, `case_id`, `result`, `execute_time`)
SELECT vp.`id`, tc.`id`, IF(RAND() < 0.8, '成功', '失败'), vp.`create_time`
FROM `version_plan` vp
JOIN `test_case` tc ON tc.`project_id` = vp.`project_id`
WHERE vp.`name` LIKE '批量V%';

-- 5. 回填每日统计汇总表（含历史手工计划已执行的数据）
INSERT INTO `execution_daily_stat` (`stat_date`, `project_id`, `test_team`, `executed_count`, `success_count`, `fail_count`)
SELECT
  DATE(FROM_UNIXTIME(vp.`create_time` / 1000)),
  vp.`project_id`,
  MAX(p.`test_team`),
  COUNT(*),
  SUM(pc.`result` = '成功'),
  SUM(pc.`result` = '失败')
FROM `plan_case` pc
JOIN `version_plan` vp ON vp.`id` = pc.`plan_id`
JOIN `project` p ON p.`id` = vp.`project_id`
WHERE pc.`result` IN ('成功', '失败')
GROUP BY DATE(FROM_UNIXTIME(vp.`create_time` / 1000)), vp.`project_id`;
