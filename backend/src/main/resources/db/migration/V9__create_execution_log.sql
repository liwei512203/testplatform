-- =========================================================
-- 执行事实日志表（append-only）：
--   每次一键执行，为计划下每条用例追加一行执行记录。
--   语义：计划重复执行 = 用例当天被执行多次，统计自然累加。
--   plan_case 退化为"当前状态表"（展示最新一次结果），
--   报表统计全部从本日志表聚合。
-- 按月分区（exec_date 为分区键，包含在主键中），保留期与 plan_case 一致
-- =========================================================

CREATE TABLE `case_execution_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `plan_id` bigint NOT NULL COMMENT '版本计划ID',
  `project_id` varchar(50) NOT NULL COMMENT '项目ID',
  `case_id` bigint NOT NULL COMMENT '测试用例ID',
  `result` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '执行结果：成功/失败',
  `execute_time` bigint NOT NULL COMMENT '执行时间（与版本计划创建时间保持一致）',
  `exec_date` date NOT NULL COMMENT '执行日期（分区键，= 执行时间对应日期）',
  PRIMARY KEY (`id`, `exec_date`),
  KEY `idx_plan`(`plan_id`),
  KEY `idx_project_date`(`project_id`, `exec_date`),
  KEY `idx_case`(`case_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用例执行日志（报表统计事实来源）'
PARTITION BY RANGE COLUMNS(`exec_date`) (
  PARTITION p202512 VALUES LESS THAN ('2026-01-01'),
  PARTITION p202601 VALUES LESS THAN ('2026-02-01'),
  PARTITION p202602 VALUES LESS THAN ('2026-03-01'),
  PARTITION p202603 VALUES LESS THAN ('2026-04-01'),
  PARTITION p202604 VALUES LESS THAN ('2026-05-01'),
  PARTITION p202605 VALUES LESS THAN ('2026-06-01'),
  PARTITION p202606 VALUES LESS THAN ('2026-07-01'),
  PARTITION p202607 VALUES LESS THAN ('2026-08-01'),
  PARTITION p202608 VALUES LESS THAN ('2026-09-01'),
  PARTITION p202609 VALUES LESS THAN ('2026-10-01'),
  PARTITION p202610 VALUES LESS THAN ('2026-11-01'),
  PARTITION p202611 VALUES LESS THAN ('2026-12-01'),
  PARTITION p202612 VALUES LESS THAN ('2027-01-01'),
  PARTITION p202701 VALUES LESS THAN ('2027-02-01'),
  PARTITION p202702 VALUES LESS THAN ('2027-03-01'),
  PARTITION p202703 VALUES LESS THAN ('2027-04-01'),
  PARTITION p202704 VALUES LESS THAN ('2027-05-01'),
  PARTITION p202705 VALUES LESS THAN ('2027-06-01'),
  PARTITION p202706 VALUES LESS THAN ('2027-07-01'),
  PARTITION p202707 VALUES LESS THAN ('2027-08-01'),
  PARTITION p202708 VALUES LESS THAN ('2027-09-01'),
  PARTITION p202709 VALUES LESS THAN ('2027-10-01'),
  PARTITION p202710 VALUES LESS THAN ('2027-11-01'),
  PARTITION p202711 VALUES LESS THAN ('2027-12-01'),
  PARTITION p202712 VALUES LESS THAN ('2028-01-01'),
  PARTITION pmax VALUES LESS THAN (MAXVALUE)
);

-- 回填历史执行事实（plan_case 已执行的行 = 历史上各执行了一次）
INSERT INTO `case_execution_log`
  (`plan_id`, `project_id`, `case_id`, `result`, `execute_time`, `exec_date`)
SELECT pc.`plan_id`, vp.`project_id`, pc.`case_id`, pc.`result`, pc.`execute_time`, pc.`plan_date`
FROM `plan_case` pc
JOIN `version_plan` vp ON vp.`id` = pc.`plan_id`
WHERE pc.`result` IN ('成功', '失败');
