-- =========================================================
-- 用例执行每日统计（用例粒度）
-- 支撑：某时间段内某项目/某团队下各测试用例的执行次数、
-- 成功率排行（如"成功率最低的用例"）等分析
-- 行数规模 ≈ 每天实际被执行过的用例数，远小于 plan_case 明细
-- =========================================================

CREATE TABLE `execution_case_daily_stat` (
  `stat_date` date NOT NULL COMMENT '统计日期',
  `project_id` varchar(50) NOT NULL COMMENT '项目ID',
  `case_id` bigint NOT NULL COMMENT '测试用例ID',
  `test_team` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属测试团队（冗余，便于团队维度下钻）',
  `executed_count` bigint NOT NULL DEFAULT 0 COMMENT '执行次数（成功+失败）',
  `success_count` bigint NOT NULL DEFAULT 0 COMMENT '执行成功次数',
  `fail_count` bigint NOT NULL DEFAULT 0 COMMENT '执行失败次数',
  PRIMARY KEY (`stat_date`, `case_id`),
  KEY `idx_project_date`(`project_id`, `stat_date`),
  KEY `idx_team_date`(`test_team`, `stat_date`),
  KEY `idx_case_fail`(`case_id`, `fail_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用例执行每日统计（用例粒度，报表数据源）';

-- 回填历史数据（从 plan_case 明细按 天×项目×用例 聚合）
INSERT INTO `execution_case_daily_stat`
  (`stat_date`, `project_id`, `case_id`, `test_team`, `executed_count`, `success_count`, `fail_count`)
SELECT
  DATE(FROM_UNIXTIME(vp.`create_time` / 1000)),
  vp.`project_id`,
  pc.`case_id`,
  MAX(p.`test_team`),
  COUNT(*),
  SUM(pc.`result` = '成功'),
  SUM(pc.`result` = '失败')
FROM `plan_case` pc
JOIN `version_plan` vp ON vp.`id` = pc.`plan_id`
JOIN `project` p ON p.`id` = vp.`project_id`
WHERE pc.`result` IN ('成功', '失败')
GROUP BY DATE(FROM_UNIXTIME(vp.`create_time` / 1000)), vp.`project_id`, pc.`case_id`;
